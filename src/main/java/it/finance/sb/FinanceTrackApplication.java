package it.finance.sb;

import it.finance.sb.io.CsvImporter;
import it.finance.sb.io.CsvTransactionImporter;
import it.finance.sb.io.CsvWriter;
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

public class FinanceTrackApplication {

    // Services
    private static final TransactionService transactionService = new TransactionService();
    private static final UserService userService = new UserService();
    private static final AccountService accountService = new AccountService(transactionService);
    private static final CsvImporter<AbstractTransaction> importer = new CsvTransactionImporter();
    private static final CsvWriter<AbstractTransaction> writer = new CsvWriter<>("TransactionId,Type,Amount,From,To,Category,Reason,Date");
    private static final FileIOService fileIOService = new FileIOService(transactionService, userService, importer, writer);
    private static final InvestmentService investmentService = new InvestmentService(accountService, userService);
    private static final MementoService mementoService = new MementoService();

    private static User currentUser;

    public static void main(String[] args) {
        System.out.println(ConsoleStyle.header("Welcome to 💸 FinanceTrack!"));
        loginUser();
        showMainMenu();
        System.out.println(ConsoleStyle.info("Thank you for using FinanceTrack!"));
    }

    private static void loginUser() {
        System.out.println(ConsoleStyle.section("🔐 Load or Create User"));

        int choice = ConsoleUtils.showMenu("User Login", "Load existing user", "Create new user");
        if (choice == 1) {
            List<String> savedUsers = mementoService.listUsers();
            if (savedUsers.isEmpty()) {
                System.out.println(ConsoleStyle.warning("⚠️ No saved users found."));
                createNewUser();
            } else {
                int selected = ConsoleUtils.showMenu("Select a saved user", savedUsers.toArray(new String[0]));
                try {
                    mementoService.loadUser(savedUsers.get(selected - 1)).ifPresentOrElse(user -> {
                        currentUser = user;
                        System.out.println(ConsoleStyle.success("✅ Loaded user: " + user.getName()));
                    }, () -> createNewUser());
                } catch (Exception e) {
                    System.out.println(ConsoleStyle.error("❌ Failed to load user: " + e.getMessage()));
                }
            }
        } else {
            createNewUser();
        }

        // Set current user on all services
        userService.setCurrentUser(currentUser);
        accountService.setCurrentUser(currentUser);
        transactionService.setCurrentUser(currentUser);
        fileIOService.setCurrentUser(currentUser);
        investmentService.setCurrentUser(currentUser);
    }

    private static void createNewUser() {
        try {
            String name = ConsoleUtils.prompt("Enter your name", false);
            int age = Integer.parseInt(ConsoleUtils.prompt("Enter your age", false));
            Gender gender = ConsoleUtils.selectEnum(Gender.class, "Select gender", false);
            currentUser = userService.create(name, age, gender);
            mementoService.saveUser(currentUser);
            System.out.println(ConsoleStyle.success("💾 User created successfully!"));
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error("❌ Failed to create user: " + e.getMessage()));
        }
    }

    private static void showMainMenu() {
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
                        System.out.println(ConsoleStyle.warning("⚠️ No accounts available. Please create one."));
                    } else {
                        showTransactionMenu();
                    }
                }
                case 3 -> showCsvMenu();
                case 4 ->
                        System.out.println(ConsoleStyle.warning("⚠️ Feature will be implemented."));//investmentService.showInvestmentMenu();
                case 5 -> {
                    try {
                        mementoService.saveUser(currentUser);
                        System.out.println(ConsoleStyle.success("✅ User saved!"));
                    } catch (Exception e) {
                        System.out.println(ConsoleStyle.error("❌ Failed to save user: " + e.getMessage()));
                    }
                }
                case 6 -> running = false;
                default -> System.out.println(ConsoleStyle.error("Invalid option."));
            }
        }
    }

    // ===================== ACCOUNT MENU =====================
    private static void showAccountMenu() {
        boolean running = true;
        while (running) {
            int choice = ConsoleUtils.showMenu("Account Menu",
                    "View Accounts",
                    "Create Account",
                    "Update Account",
                    "Delete Account",
                    "Back");

            switch (choice) {
                case 1 -> accountService.displayAllAccount();
                case 2 -> createNewAccount();
                case 3 -> updateAccount();
                case 4 -> deleteAccount();
                case 5 -> running = false;
            }
        }
    }

    private static void createNewAccount() {
        try {
            String name = ConsoleUtils.prompt("Account name", false);
            Double balance = ConsoleUtils.promptForDouble("Initial balance", false);
            AccounType type = ConsoleUtils.selectEnum(AccounType.class, "Account Type", false);
            AccountInterface acc = accountService.create(type, name, balance);
            System.out.println(ConsoleStyle.success("✅ Created: " + acc));
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error("❌ Could not create account. Please review your inputs."));
        }
    }


    private static void updateAccount() {
        try {
            AccountInterface acc = getAccount("account to update");
            String newName = ConsoleUtils.prompt("New name (blank to skip)", true);
            AccounType newType = ConsoleUtils.selectEnum(AccounType.class, "New Type", true);
            Double newBalance = ConsoleUtils.promptForDouble("New balance (blank to skip)", true);
            accountService.modify(acc, newType, newName, newBalance);
            System.out.println(ConsoleStyle.success("✅ Account updated successfully."));
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error("❌ Could not update account. Make sure values are valid."));
        }
    }

    private static void deleteAccount() {
        try {
            AccountInterface acc = getAccount("account to delete");
            accountService.delete(acc);
            System.out.println(ConsoleStyle.success("✅ Account deleted."));
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error("❌ Could not delete account. Please try again."));
        }
    }

    // ===================== TRANSACTIONS =====================
    private static void showTransactionMenu() {
        boolean running = true;
        while (running) {
            int choice = ConsoleUtils.showMenu("Transaction Menu",
                    "View Transactions",
                    "Create Transaction",
                    "Update Transaction",
                    "Delete Transaction",
                    "Back");

            switch (choice) {
                case 1 -> displayAllTransactions();
                case 2 -> createTransaction();
                case 3 -> updateTransaction();
                case 4 -> deleteTransaction();
                case 5 -> running = false;
            }
        }
    }

    /**
     * Display all transactions for the current user (flattened list).
     */
    //TODO move away
    private static void displayAllTransactions() {

        List<AbstractTransaction> transactions = transactionService.getAllTransactionsFlattened();

        if (transactions.isEmpty()) {
            System.out.println("⚠️ No transactions found.");
            return;
        }

        System.out.println("\n📋 All Transactions:");
        for (AbstractTransaction tx : transactions) {
            System.out.printf("  ➤ ID: %-4d | 💰 Amount: %-8.2f | 📌 Category: %-12s | 📃 Reason: %-20s | 📅 Date: %s | Type: %s\n",
                    tx.getTransactionId(),
                    tx.getAmount(),
                    tx.getCategory(),
                    tx.getReason(),
                    tx.getDate(),
                    tx.getType().name()
            );
        }
    }

    private static void createTransaction() {
        try {
            TransactionType type = ConsoleUtils.selectEnum(TransactionType.class, "Transaction Type", false);
            Double amount = ConsoleUtils.promptForDouble("Amount", false);
            String category = ConsoleUtils.selectOrCreateCategory(currentUser.getSortedCategories(), false);
            String reason = ConsoleUtils.prompt("Reason", false);
            AccountInterface[] acc = getTransactionAccounts(type);
            transactionService.create(type, amount, category, reason, new Date(), acc[0], acc[1]);
            System.out.println(ConsoleStyle.success("✅ Transaction created."));
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error("❌ Failed to create transaction. Make sure input is valid."));
        }
    }

    private static void updateTransaction() {
        try {
            AbstractTransaction tx = getTransaction("to update");
            Double newAmount = ConsoleUtils.promptForDouble("New amount (blank to skip)", true);
            String newCategory = ConsoleUtils.selectOrCreateCategory(currentUser.getSortedCategories(), true);
            String newReason = ConsoleUtils.prompt("New reason (blank to skip)", true);
            AccountInterface[] acc = getTransactionAccounts(tx.getType());
            transactionService.modify(tx, newAmount, newCategory, newReason, new Date(), acc[0], acc[1]);
            System.out.println(ConsoleStyle.success("✅ Transaction updated."));
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error("❌ Could not update transaction."));
        }
    }

    private static void deleteTransaction() {
        try {
            AbstractTransaction tx = getTransaction("to delete");
            transactionService.delete(tx);
            System.out.println(ConsoleStyle.success("✅ Transaction deleted."));
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error("❌ Could not delete transaction."));
        }
    }

    // ===================== FILE IO =====================
    private static void showCsvMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println(ConsoleStyle.menuTitle("CSV Menu"));
            System.out.println("1️⃣  Import Transactions");
            System.out.println("2️⃣  Export Transactions");
            System.out.println("3️⃣  Back");

            switch (scanner.nextLine().trim()) {
                case "1" -> importTransactions();
                case "2" -> exportTransactions();
                case "3" -> running = false;
            }
        }
    }

    private static void importTransactions() {
        try {
            Path path = Path.of(ConsoleUtils.prompt("Enter CSV path", false));
            boolean autoCreate = ConsoleUtils.prompt("Auto-create missing accounts? (y/n)", false).equalsIgnoreCase("y");
            boolean skipErrors = ConsoleUtils.prompt("Skip errors? (y/n)", false).equalsIgnoreCase("y");
            fileIOService.importTransactions(path, autoCreate, skipErrors);
            System.out.println(ConsoleStyle.success("✅ Transactions imported."));
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error("❌ Failed to import. Check file path and content format."));
        }
    }

    private static void exportTransactions() {
        try {
            Path path = Path.of(ConsoleUtils.prompt("Enter output CSV path", false));
            fileIOService.exportTransactions(path);
            System.out.println(ConsoleStyle.success("✅ Export completed to: " + path));
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error("❌ Could not export transactions. Verify output path."));
        }
    }

    // ===================== HELPERS =====================
    private static AccountInterface getAccount(String label) {
        accountService.displayAllAccount();
        String input = ConsoleUtils.prompt("Enter " + label + " index", false);
        int index = Integer.parseInt(input.trim()) - 1;
        return currentUser.getAccountList().get(index);
    }

    private static AbstractTransaction getTransaction(String label) {
        List<AbstractTransaction> txs = transactionService.getAllTransactionsFlattened();
        for (int i = 0; i < txs.size(); i++) {
            System.out.println((i + 1) + ". " + txs.get(i));
        }
        String input = ConsoleUtils.prompt("Enter transaction " + label + " index", false);
        return txs.get(Integer.parseInt(input.trim()) - 1);
    }

    private static AccountInterface[] getTransactionAccounts(TransactionType type) {
        AccountInterface from = null, to = null;
        if (type == TransactionType.INCOME) to = getAccount("destination");
        if (type == TransactionType.EXPENSE) from = getAccount("source");
        if (type == TransactionType.MOVEMENT) {
            from = getAccount("source");
            to = getAccount("destination");
        }
        return new AccountInterface[]{to, from};
    }
}
