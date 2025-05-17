package it.finance.sb;

import it.finance.sb.exception.MementoException;
import it.finance.sb.exception.UserLoginException;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionType;
import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;
import it.finance.sb.service.*;
import it.finance.sb.utility.ConsoleStyle;
import it.finance.sb.utility.ConsoleUtils;

import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class FinanceTrackApplicationNew {
    private static final TransactionService transactionService = new TransactionService();
    private static final UserService userService = new UserService();
    private static final AccountService accountService = new AccountService(transactionService);
    private static final FileIOService fileIOService = new FileIOService(transactionService);
    private static final InvestmentService investmentService = new InvestmentService(accountService, userService);
    private static final MementoService mementoService = new MementoService();

    private static User currentUser;

    public static void main(String[] args) throws UserLoginException {
        System.out.println(ConsoleStyle.header("Welcome to 💸 FinanceTrack!"));
        loginUser();
        showMainMenu();
        System.out.println(ConsoleStyle.info("Thank you for using FinanceTrack!"));
    }

    private static void loginUser() {
        System.out.println(ConsoleStyle.section("🔐 Load or Create User"));

        int choice = ConsoleUtils.showMenu("User Login",
                "Load existing user",
                "Create new user");

        if (choice == 1) {
            List<String> savedUsers = mementoService.listUsers();
            if (savedUsers.isEmpty()) {
                System.out.println(ConsoleStyle.warning("⚠️ No saved users found. Switching to new user creation."));
                createNewUser();
            } else {
                int selected = ConsoleUtils.showMenu("Select a saved user", savedUsers.toArray(new String[0]));
                String username = savedUsers.get(selected - 1);

                try {
                    mementoService.loadUser(username).ifPresentOrElse(user -> {
                        currentUser = user;
                        System.out.println(ConsoleStyle.success("✅ Loaded user: " + currentUser.getName()));
                    }, () -> {
                        System.out.println(ConsoleStyle.error("❌ Could not load user. Creating new one."));
                        createNewUser();
                    });
                } catch (MementoException e) {
                    System.out.println(ConsoleStyle.error("❌ Error during loading the user. See logs for details."));
                }
            }

        } else {
            createNewUser();
        }

        // Bind user to services
        userService.setCurrentUser(currentUser);
        accountService.setCurrentUser(currentUser);
        investmentService.setCurrentUser(currentUser);
        transactionService.setCurrentUser(currentUser);
        fileIOService.setCurrentUser(currentUser);
    }

    //Mememento helper
    private static void createNewUser() {
        String name = ConsoleUtils.prompt("Enter your name", false);
        int age = Integer.parseInt(ConsoleUtils.prompt("Enter your age", false));
        Gender gender = ConsoleUtils.selectEnum(Gender.class, "Select gender", false);

//TODO catch different exception
        try {
            currentUser = userService.create(name, age, gender);
            mementoService.saveUser(currentUser);
            System.out.println(ConsoleStyle.success("💾 User created successfully!"));
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error("❌ Could not save user. See logs for details."));
        }
    }

    private static void showMainMenu() throws UserLoginException {
        boolean running = true;

        while (running) {
            int choice = ConsoleUtils.showMenu("Main Menu",
                    "Manage Accounts",
                    "Manage Transactions",
                    "Import/Export CSV",
                    "Investment Calculator",
                    "Save Current User",
                    "Exit");

            switch (choice) {
                case 1 -> showAccountMenu();
                case 2 -> {
                    if (currentUser.getAccountList().isEmpty()) {
                        System.out.println(ConsoleStyle.warning("You must create an account first."));
                    } else {
                        showTransactionMenu();
                    }
                }
                case 3 -> showCsvMenu();
                case 4 -> investmentService.showInvestmentMenu();
                case 5 -> {
                    try {
                        mementoService.saveUser(currentUser);
                        System.out.println(ConsoleStyle.success("💾 User saved successfully!"));
                    } catch (Exception e) {
                        System.out.println(ConsoleStyle.error("❌ Could not save user. See logs for details."));
                    }
                }
                case 6 -> {
                    running = false;
                    System.out.println(ConsoleStyle.info("Exiting FinanceTrack..."));
                }
                default -> System.out.println(ConsoleStyle.error("Invalid option."));
            }
        }
    }

    //Account helpers flow
    private static void showAccountMenu() {
        boolean running = true;
        while (running) {
            int choice = ConsoleUtils.showMenu("Account Menu",
                    "View Accounts",
                    "Create New Account",
                    "Update Account",
                    "Delete Account",
                    "Back to Main Menu");

            switch (choice) {
                case 1 -> {
                    if (currentUser.getAccountList().isEmpty()) {
                        System.out.println(ConsoleStyle.warning("No accounts found. Please create one."));
                    } else {
                        userService.displayAllAccount();
                        System.out.println(ConsoleStyle.info("Press Enter to continue..."));
                        new Scanner(System.in).nextLine();
                    }
                }
                case 2 -> createNewAccount();
                case 3 -> performAccountUpdate();
                case 4 -> deleteAccount();
                case 5 -> running = false;
                default -> System.out.println(ConsoleStyle.error("Invalid option. Try again."));
            }
        }
    }

    private static void createNewAccount() {
        System.out.println(ConsoleStyle.section("Create New Account"));

        String name = ConsoleUtils.prompt("Account name", false);
        Double balance = ConsoleUtils.promptForDouble("Initial balance", false);
        AccounType type = ConsoleUtils.selectEnum(AccounType.class, "Account Type", false);

        try {
            AccountInterface account = accountService.create(type, name, balance);
            System.out.println(ConsoleStyle.success("✅ Created: " + account));
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error("❌ Error: " + e.getMessage()));
        }
    }

    private static AccountInterface getAccountId(String label, boolean canEmpty, AccountInterface exclude) {
        userService.displayAllAccount(exclude);
        String input = ConsoleUtils.prompt("Enter " + label + " Account ID", canEmpty);

        if (input.isBlank() && canEmpty) return null;

        try {
            int index = Integer.parseInt(input.trim()) - 1;
            AccountInterface selected = currentUser.getAccountList().get(index);

            if (selected.equals(exclude)) {
                System.out.println(ConsoleStyle.warning("You can't select the same account for both FROM and TO."));
                return getAccountId(label, canEmpty, exclude);
            }

            return selected;
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error("Invalid account ID."));
            return getAccountId(label, canEmpty, exclude);
        }
    }

    private static void performAccountUpdate() {
        System.out.println(ConsoleStyle.section("Update Account"));

        AccountInterface account = getAccountId(" ", false, null);
        String newName = ConsoleUtils.prompt("New name (or leave blank)", true);
        AccounType newType = ConsoleUtils.selectEnum(AccounType.class, "New Account Type", true);
        Double newBalance = ConsoleUtils.promptForDouble("New balance", true);

        try {
            accountService.modify(account, newType, newName, newBalance);
            System.out.println(ConsoleStyle.success("✅ Updated: " + account));
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error("❌ Error updating account: " + e.getMessage()));
        }
    }

    private static void deleteAccount() {
        System.out.println(ConsoleStyle.section("Delete Account"));

        AccountInterface account = getAccountId(" ", false, null);
        try {
            AccountInterface deleted = accountService.delete(account);
            System.out.println(ConsoleStyle.success("✅ Deleted: " + deleted));
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error("❌ Error deleting account: " + e.getMessage()));
        }
    }

    // Transaction Helper
    private static void showTransactionMenu() {
        boolean running = true;
        while (running) {
            int choice = ConsoleUtils.showMenu("Transaction Menu",
                    "View All Transactions",
                    "Add Transaction",
                    "Modify Transaction",
                    "Delete Transaction",
                    "Back to Main Menu");

            switch (choice) {
                case 1 -> transactionService.displayAllTransactions();
                case 2 -> createTransactionCLI();
                case 3 -> performTransactionUpdate();
                case 4 -> deleteTransaction();
                case 5 -> running = false;
                default -> System.out.println(ConsoleStyle.error("Invalid choice."));
            }
        }
    }

    private static void createTransactionCLI() {
        System.out.println(ConsoleStyle.section("Create Transaction"));

        TransactionType type = ConsoleUtils.selectEnum(TransactionType.class, "Transaction Type", false);
        Double amount = ConsoleUtils.promptForDouble("Amount", false);
        String category = ConsoleUtils.selectOrCreateCategory(currentUser.getSortedCategories(), false);
        if (category != null && !currentUser.isCategoryAllowed(category)) {
            userService.addCategory(category);
            System.out.println(ConsoleStyle.info("📁 New category added: " + category));
        }
        String reason = ConsoleUtils.prompt("Reason", false);
        AccountInterface[] accounts = getTransactionAccount(type, false);

        try {
            transactionService.create(type, amount, category, reason, new Date(), accounts[0], accounts[1]);
            System.out.println(ConsoleStyle.success("Transaction created successfully."));
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error("Error: " + e.getMessage()));
        }
    }

    private static AccountInterface[] getTransactionAccount(TransactionType type, boolean canEmpty) {
        AccountInterface from = null, to = null;

        if (type == TransactionType.INCOME) {
            to = getAccountId("TO", canEmpty, null);
        } else if (type == TransactionType.EXPENSE) {
            from = getAccountId("FROM", canEmpty, null);
        } else if (type == TransactionType.MOVEMENT) {
            from = getAccountId("FROM", canEmpty, null);
            to = getAccountId("TO", canEmpty, from);
        }

        return new AccountInterface[]{to, from};
    }

    private static void performTransactionUpdate() {
        System.out.println(ConsoleStyle.section("Update Transaction"));

        AbstractTransaction transaction = getTransactionId(" ");

        Double newAmount = ConsoleUtils.promptForDouble("New amount", true);
        String newCategory = ConsoleUtils.selectOrCreateCategory(currentUser.getSortedCategories(), true);
        String newReason = ConsoleUtils.prompt("New reason", true);
        AccountInterface[] accounts = getTransactionAccount(transaction.getType(), true);

        try {
            transactionService.modify(transaction, newAmount, newCategory, newReason, new Date(), accounts[0], accounts[1]);
            System.out.println(ConsoleStyle.success("✅ Transaction updated: " + transaction));
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error("❌ Failed to update transaction: " + e.getMessage()));
        }
    }

    private static void deleteTransaction() {
        System.out.println(ConsoleStyle.section("Delete Transaction"));

        AbstractTransaction tx = getTransactionId(" ");
        try {
            transactionService.delete(tx);
            System.out.println(ConsoleStyle.success("✅ Transaction deleted: " + tx));
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error("❌ Failed to delete transaction: " + e.getMessage()));
        }
    }

    private static AbstractTransaction getTransactionId(String label) {
        transactionService.displayAllTransactions();

        String input = ConsoleUtils.prompt("Enter" + label + " transaction ID", false);
        try {
            int index = Integer.parseInt(input.trim()) - 1;
            return transactionService.getAllTransactionsFlattened().get(index);
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error("Invalid transaction ID."));
            return getTransactionId(label);
        }
    }

    //File IO helper
    private static void showCsvMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println(ConsoleStyle.menuTitle("CSV Import/Export"));
            System.out.println("1️⃣  Import Transactions from CSV");
            System.out.println("2️⃣  Export Transactions to CSV");
            System.out.println("3️⃣  Back to Main Menu");
            System.out.print("Select an option: ");

            switch (scanner.nextLine()) {
                case "1" -> handleCsvImport(scanner);
                case "2" -> handleCsvExport(scanner);
                case "3" -> running = false;
                default -> System.out.println(ConsoleStyle.warning("Invalid option. Try again."));
            }
        }
    }

    private static void handleCsvImport(Scanner scanner) {
        System.out.println(ConsoleStyle.section("📥 Import Transactions from CSV"));

        System.out.print("Enter path to CSV file: ");
        String pathStr = scanner.nextLine();

        System.out.print("Auto-create missing accounts? (y/n): ");
        boolean autoCreate = scanner.nextLine().trim().equalsIgnoreCase("y");

        System.out.print("Skip bad lines? (y/n): ");
        boolean skipErrors = scanner.nextLine().trim().equalsIgnoreCase("y");

        try {
            fileIOService.importTransactions(Path.of(pathStr), autoCreate, skipErrors);
            System.out.println(ConsoleStyle.success("✅ Transactions imported successfully!"));
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error("❌ Import failed: " + e.getMessage()));
        }
    }

    private static void handleCsvExport(Scanner scanner) {
        System.out.println(ConsoleStyle.section("📤 Export Transactions to CSV"));

        System.out.print("Enter path to export file (e.g., `output.csv`): ");
        String pathStr = scanner.nextLine();

        try {
            fileIOService.exportTransactions(Path.of(pathStr));
            System.out.println(ConsoleStyle.success("✅ Transactions exported successfully to " + pathStr));
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error("❌ Export failed: " + e.getMessage()));
        }
    }

}
