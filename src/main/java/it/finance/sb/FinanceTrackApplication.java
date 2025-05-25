package it.finance.sb;

import it.finance.sb.exception.*;
import it.finance.sb.io.CsvImporter;
import it.finance.sb.io.ImporterI;
import it.finance.sb.io.CsvWriter;
import it.finance.sb.io.WriterI;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionType;
import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;
import it.finance.sb.service.*;
import it.finance.sb.utility.ConsoleStyle;
import it.finance.sb.utility.ConsoleUtils;
import it.finance.sb.utility.TransactionPrinter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FinanceTrackApplication {

    private static final UserService userService = new UserService();
    private static final TransactionService transactionService = new TransactionService(userService);
    private static final AccountService accountService = new AccountService(transactionService);
    private static final ImporterI<AbstractTransaction> importer = new CsvImporter();
    private static final WriterI<AbstractTransaction> writer = new CsvWriter<>("TransactionId,Type,Amount,From,To,Category,Reason,Date");
    private static final FileIOService fileIOService = new FileIOService(transactionService, userService, importer, writer);
    private static final InvestmentService investmentService = new InvestmentService(accountService, userService);
    private static final MementoService mementoService = new MementoService();

    private static final Logger logger = LoggerFactory.getInstance().getLogger(FinanceTrackApplication.class);
    private static User currentUser;

    public static void main(String[] args) throws UserCancelledException {
        System.out.println(ConsoleStyle.header("Welcome to 💸 FinanceTrack!"));
        loginUser();
        showMainMenu();
        System.out.println(ConsoleStyle.info("Thank you for using FinanceTrack!"));
    }

    private static void loginUser() {
        System.out.println(ConsoleStyle.section("🔐 Load or Create User"));
        try {
            int loginChoice = ConsoleUtils.showMenu("User Login", false, "Load existing user", "Create new user");
            if (loginChoice == 1) {
                List<String> savedUsers = mementoService.listUsers();
                if (savedUsers.isEmpty()) {
                    System.out.println(ConsoleStyle.warning(" No saved users found."));
                    createNewUser();
                } else {
                    int selected = ConsoleUtils.showMenu("Select a saved user", savedUsers.toArray(new String[0]));
                    if (selected == -1) throw new UserCancelledException(); // user backed out
                    mementoService.loadUser(savedUsers.get(selected - 1)).ifPresentOrElse(user -> {
                        currentUser = user;
                        System.out.println(ConsoleStyle.success(" Loaded user: " + user.getName()));
                    }, FinanceTrackApplication::createNewUser);

                }
            } else {
                createNewUser();
            }
            userService.setCurrentUser(currentUser);
            accountService.setCurrentUser(currentUser);
            transactionService.setCurrentUser(currentUser);
            fileIOService.setCurrentUser(currentUser);
            investmentService.setCurrentUser(currentUser);
        } catch (MementoException e) {
            logger.log(Level.SEVERE, " Failed to load user", e);
            System.out.println(ConsoleStyle.error(" Failed to load user."));
            loginUser();
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(" Operation cancelled."));
            loginUser();
        }
    }


    private static void createNewUser() {
        try {
            String name = ConsoleUtils.prompt("Enter your name", false);
            if (name == null) throw new UserCancelledException();

            String ageStr = ConsoleUtils.prompt("Enter your age", false);
            if (ageStr == null) throw new UserCancelledException();
            int age = Integer.parseInt(ageStr);

            Gender gender = ConsoleUtils.selectEnum(Gender.class, "Select gender", false);
            if (gender == null) throw new UserCancelledException();

            currentUser = userService.create(name, age, gender);
            mementoService.saveUser(currentUser);
            System.out.println(ConsoleStyle.success(" User created and saved!"));

        } catch (DataValidationException e) {
            logger.warning("User data invalid: " + e.getMessage());
            System.out.println(ConsoleStyle.error(" Invalid user data: " + e.getMessage()));
            createNewUser();
        } catch (NumberFormatException e) {
            logger.warning("User data invalid: " + e.getMessage());
            System.out.println(ConsoleStyle.error(" Invalid age number format."));
            createNewUser();
        } catch (MementoException e) {
            logger.severe("Failed to save user: " + e.getMessage());
            System.out.println(ConsoleStyle.error(" Could not save user."));
            createNewUser();
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(" Operation cancelled."));
            loginUser(); // go back to user menu
        }
    }

    private static void showMainMenu() throws UserCancelledException {
        boolean running = true;
        while (running) {
            int choice = ConsoleUtils.showMenu("Main Menu", false,
                    "Manage Accounts",
                    "Manage Transactions",
                    "Import/Export CSV",
                    "Investment Calculator",
                    "Save Current User",
                    "Exit");
            if (choice == -1) return; // user backed out
            switch (choice) {
                case 1 -> showAccountMenu();
                case 2 -> showTransactionMenu();
                case 3 -> showCsvMenu();
                case 4 -> System.out.println(ConsoleStyle.warning(" Investment feature coming soon."));
                case 5 -> {
                    try {
                        mementoService.saveUser(currentUser);
                        System.out.println(ConsoleStyle.success(" User saved."));
                    } catch (Exception e) {
                        System.out.println(ConsoleStyle.error(" Failed to save user."));
                        logger.log(Level.SEVERE, "User save failed", e);
                    }
                }
                case 6 -> running = false;
            }
        }
    }

    // ================= ACCOUNT ===================

    private static void showAccountMenu() throws UserCancelledException {
        boolean running = true;
        while (running) {
            int choice = ConsoleUtils.showMenu("Account Menu", false,
                    "View Accounts", "Create Account", "Update Account", "Delete Account", "Back");
            if (choice == -1) return; // user backed out
            switch (choice) {
                case 1 -> accountService.displayAllAccount();
                case 2 -> createAccount();
                case 3 -> updateAccount();
                case 4 -> deleteAccount();
                case 5 -> running = false;
            }
        }
    }

    private static void createAccount() {
        try {
            String name = ConsoleUtils.prompt("Account name", false);

            Double balance = ConsoleUtils.promptForDouble("Initial balance", false);

            AccounType type = ConsoleUtils.selectEnum(AccounType.class, "Account Type", false);

            AccountInterface acc = accountService.create(type, name, balance);
            System.out.println(ConsoleStyle.success(" Created: " + acc));

        } catch (DataValidationException e) {
            logger.warning("Invalid account data: " + e.getMessage());
            System.out.println(ConsoleStyle.error(" Check account details: " + e.getMessage()));
        } catch (AccountOperationException | UserLoginException e) {
            logger.warning("Account creation failed: " + e.getMessage());
            System.out.println(ConsoleStyle.error(" " + e.getMessage()));
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(" Operation cancelled."));
        }
    }

    private static void updateAccount() {
        try {
            AccountInterface acc = getAccount("to update");
            String name = ConsoleUtils.prompt("New name (blank to skip)", true);
            AccounType type = ConsoleUtils.selectEnum(AccounType.class, "New Type", true);
            Double balance = ConsoleUtils.promptForDouble("New balance (blank to skip)", true);

            accountService.modify(acc, type, name, balance);
            System.out.println(ConsoleStyle.success(" Account updated."));

        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(" Operation cancelled."));
        } catch (AccountOperationException | UserLoginException | DataValidationException e) {
            logger.warning("Account update error: " + e.getMessage());
            System.out.println(ConsoleStyle.error(" Could not update account: " + e.getMessage()));
        }
    }

    private static void deleteAccount() {
        try {
            AccountInterface acc = getAccount("to delete");
            accountService.delete(acc);
            System.out.println(ConsoleStyle.success(" Account deleted."));
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(" Operation cancelled."));
        } catch (AccountOperationException | UserLoginException e) {
            logger.warning("Delete failed: " + e.getMessage());
            System.out.println(ConsoleStyle.error(" " + e.getMessage()));
        }
    }

    // ================= TRANSACTION ===================

    private static void showTransactionMenu() throws UserCancelledException {
        boolean running = true;
        while (running) {
            int choice = ConsoleUtils.showMenu("Transaction Menu", false,
                    "View Transactions", "Create Transaction", "Update Transaction", "Delete Transaction", "Back");
            if (choice == -1) return; // user backed out
            switch (choice) {
                case 1 -> displayAllTransactions();
                case 2 -> createTransaction();
                case 3 -> updateTransaction();
                case 4 -> deleteTransaction();
                case 5 -> running = false;
            }
        }
    }

    private static void createTransaction() {
        try {
            if (currentUser.getAccountList().isEmpty())
                throw new TransactionOperationException("At least one account is required.");

            TransactionType type = ConsoleUtils.selectEnum(TransactionType.class, "Transaction Type", false);

            Double amount = ConsoleUtils.promptForDouble("Amount", false);
            String category = ConsoleUtils.selectOrCreateCategory(currentUser.getSortedCategories(), false);
            String reason = ConsoleUtils.prompt("Reason", false);
            AccountInterface[] acc = getTransactionAccounts(type);

            transactionService.create(type, amount, category, reason, new Date(), acc[0], acc[1]);
            System.out.println(ConsoleStyle.success("✅ Transaction created."));

        } catch (TransactionOperationException | DataValidationException | UserLoginException e) {
            logger.warning("Transaction error: " + e.getMessage());
            System.out.println(ConsoleStyle.error(" " + e.getMessage()));
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(" Cancelled by user."));
        }
    }

    private static void updateTransaction() {
        try {
            AbstractTransaction tx = getTransaction("to update");
            Double amount = ConsoleUtils.promptForDouble("New amount (blank to skip)", true);
            String category = ConsoleUtils.selectOrCreateCategory(currentUser.getSortedCategories(), true);
            String reason = ConsoleUtils.prompt("New reason (blank to skip)", true);
            AccountInterface[] acc = getTransactionAccounts(tx.getType());

            transactionService.modify(tx, amount, category, reason, new Date(), acc[0], acc[1]);
            System.out.println(ConsoleStyle.success(" Transaction updated."));

        } catch (TransactionOperationException | UserLoginException e) {
            System.out.println(ConsoleStyle.error(" Could not update: " + e.getMessage()));
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(" Operation cancelled."));
        }
    }

    private static void deleteTransaction() {
        try {
            AbstractTransaction tx = getTransaction("to delete");
            transactionService.delete(tx);
            System.out.println(ConsoleStyle.success(" Transaction deleted."));
        } catch (TransactionOperationException | UserLoginException e) {
            System.out.println(ConsoleStyle.error(" Could not delete: " + e.getMessage()));
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(" Operation cancelled."));
        }
    }

    private static void displayAllTransactions() {
        List<AbstractTransaction> list = transactionService.getAllTransactionsFlattened();
        if (list.isEmpty()) {
            System.out.println(ConsoleStyle.warning(" No transactions available."));
        } else {
            TransactionPrinter.printTransactions(list);
        }
    }

    // ================= FILE ===================
    private static void showCsvMenu() throws UserCancelledException {
        boolean running = true;
        while (running) {
            int choice = ConsoleUtils.showMenu("CSV Menu", false, "Import Transactions", "Export Transactions", "Back");
            if (choice == -1) return; // user backed out
            switch (choice) {
                case 1 -> importTransactions();
                case 2 -> exportTransactions();
                case 3 -> running = false;
            }
        }
    }

    private static void importTransactions() {
        try {
            Path path = Path.of(ConsoleUtils.prompt("Enter CSV path", false));
            boolean autoCreate = ConsoleUtils.prompt("Auto-create missing accounts? (y/n)", false).equalsIgnoreCase("y");
            boolean skipErrors = ConsoleUtils.prompt("Skip errors? (y/n)", false).equalsIgnoreCase("y");

            List<AbstractTransaction> imported = fileIOService.importTransactions(path, autoCreate, skipErrors);
            System.out.println(ConsoleStyle.success(" Transactions imported."));
            TransactionPrinter.printTransactions(imported);

        } catch (DataValidationException e) {
            logger.log(Level.SEVERE, "Import failed", e);
            System.out.println(ConsoleStyle.error(" Failed to import transactions. Data validation error occurred"));
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(" Operation cancelled."));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Import failed", e);
            System.out.println(ConsoleStyle.error(" Failed to import transactions. File does not exist"));
        } catch (UserLoginException e) {
            logger.log(Level.SEVERE, "Import failed", e);
            System.out.println(ConsoleStyle.error(" User must be logged in."));
        }
    }

    private static void exportTransactions() {
        try {
            Path path = Path.of(ConsoleUtils.prompt("Enter output CSV path", false));
            fileIOService.exportTransactions(path);
            System.out.println(ConsoleStyle.success(" Exported to: " + path));
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(" Operation cancelled."));
        } catch (UserLoginException e) {
            logger.log(Level.SEVERE, "Export failed", e);
            System.out.println(ConsoleStyle.error(" User must be logged in."));
        } catch (FileIOException e) {
            logger.log(Level.SEVERE, "Export failed", e);
            System.out.println(ConsoleStyle.error(" Failed to export transactions."));
        }
    }

    // =============== HELPERS ===============

    private static AccountInterface getAccount(String label) throws UserCancelledException {
        return selectFromList(currentUser.getAccountList(), label);
    }


    private static AbstractTransaction getTransaction(String label) throws UserCancelledException {
        return selectFromList(transactionService.getAllTransactionsFlattened(), "transaction " + label);
    }

    private static AccountInterface[] getTransactionAccounts(TransactionType type) throws UserCancelledException {
        AccountInterface from = null, to = null;
        switch (type) {
            case INCOME -> to = getAccount("destination");
            case EXPENSE -> from = getAccount("source");
            case MOVEMENT -> {
                from = getAccount("source");
                to = getAccount("destination");
            }
        }
        return new AccountInterface[]{to, from};
    }

    private static <T> T selectFromList(List<T> list, String promptLabel) throws UserCancelledException {
        if (list == null || list.isEmpty()) {
            System.out.println(ConsoleStyle.warning(" No items available."));
            throw new UserCancelledException();
        }

        for (int i = 0; i < list.size(); i++) {
            System.out.printf("%d. %s%n", i + 1, list.get(i).toString());
        }

        while (true) {
            String input = ConsoleUtils.prompt("Enter " + promptLabel + " index (or 'back')", false);

            if (input == null || input.equalsIgnoreCase("back")) {
                throw new UserCancelledException();
            }

            try {
                int index = Integer.parseInt(input.trim()) - 1;
                if (index >= 0 && index < list.size()) {
                    return list.get(index);
                } else {
                    System.out.println(ConsoleStyle.error(" Index out of range. Try again."));
                }
            } catch (NumberFormatException e) {
                System.out.println(ConsoleStyle.error(" Invalid number format. Enter a valid index."));
            }
        }
    }

}
