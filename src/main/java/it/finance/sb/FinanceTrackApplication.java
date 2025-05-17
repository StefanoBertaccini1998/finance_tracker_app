/*
package it.finance.sb;

import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionType;
import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;
import it.finance.sb.service.AccountService;
import it.finance.sb.service.InvestmentService;
import it.finance.sb.service.TransactionService;
import it.finance.sb.service.UserService;
import it.finance.sb.utility.ConsoleStyle;

import java.util.Date;
import java.util.List;
import java.util.Scanner;

import static it.finance.sb.model.transaction.TransactionType.*;

public class FinanceTrackApplication {
    private static User currentUser;
    private static final TransactionService transactionService = new TransactionService();
    private static final UserService userService = new UserService();
    private static final AccountService accountService = new AccountService(transactionService);
    private static final InvestmentService investmentService = new InvestmentService(accountService, userService);


    public static void main(String[] args) {
        System.out.println(ConsoleStyle.header("🎉 Welcome to 💸 FinanceTrack CLI!"));
        loginUser(); // demo login
        showMainMenu();
        System.out.println(ConsoleStyle.info("👋 Thank you for using FinanceTrack!"));
    }


    */
/**
     * Create and log in a demo user
     *//*

    private static void loginUser() {
        currentUser = new User("Stefano", 29, Gender.OTHER);
        System.out.println(ConsoleStyle.info("🔐 Logged in as: ") + ConsoleStyle.success(currentUser.getName()));

        userService.setCurrentUser(currentUser);
        accountService.setCurrentUser(currentUser);
        investmentService.setCurrentUser(currentUser);
        transactionService.setCurrentUser(currentUser);
    }

    */
/**
     * Shows the main application menu
     *//*

    private static void showMainMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println(ConsoleStyle.menuTitle("Main Menu"));
            System.out.println("1️⃣  Manage Accounts");
            System.out.println("2️⃣  Manage Transactions");
            System.out.println("3️⃣  Investment Calculator");
            System.out.println("4️⃣  🚪 Exit");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    showAccountMenu();
                    break;
                case "2":
                    if (currentUser.getAccountList().isEmpty()) {
                        System.out.println("\nError: User doesn't have any account. Create an account first!");
                        break;
                    }
                    showTransactionMenu();
                    break;
                case "3":
                    investmentService.showInvestmentMenu();
                    break;
                case "4":
                    running = false;
                    System.out.println("Exiting application...");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    */
/**
     * Shows the account management menu
     *//*

    private static void showAccountMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n===== ACCOUNT MENU =====");
            System.out.println("1. View Accounts");
            System.out.println("2. Create New Account");
            System.out.println("3. Update Account");
            System.out.println("4. Delete Account");
            System.out.println("5. Back to Main Menu");
            System.out.println("1️⃣  View Accounts");
            System.out.println("2️⃣  Create New Account");
            System.out.println("3️⃣  Update Account");
            System.out.println("4️⃣  Delete Account");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    if (currentUser.getAccountList().isEmpty()) {
                        System.out.println("\nError: User doesn't have any account. Create an account first!");
                        break;
                    }
                    userService.displayAllAccount();
                    System.out.println("\nPress Enter to continue...");
                    scanner.nextLine();
                    break;
                case "2":
                    createNewAccount(scanner);
                    break;
                case "3":
                    performAccountUpdate(scanner);
                    break;
                case "4":
                    deleteAccount(scanner);
                    break;
                case "5":
                    running = false;
                    break;

                default:
                    System.out.println("\nInvalid option. Please try again.");
            }
        }
    }

    */
/**
     * Helper method to create a new account
     *//*

    private static void createNewAccount(Scanner scanner) {
        System.out.println("\n===== CREATE NEW ACCOUNT =====");

        System.out.print("Enter account name: ");
        String name = scanner.nextLine();

        Double balance = requestForAmount(scanner, "Initial balance", false);

        AccounType type = requestForType(scanner, false);
        try {
            AccountInterface accountInterface = accountService.create(type, name, balance);
            System.out.println("\n " + accountInterface + " created successfully!");
        } catch (Exception e) {
            System.out.println("\n Error creating account: " + e.getMessage());
        }
    }

    */
/**
     * Helper method to recursive request a valid amount
     *//*

    private static Double requestForAmount(Scanner scanner, String cliLog, boolean canEmpty) {
        System.out.print("\nEnter " + cliLog + ":");
        if (canEmpty) {
            System.out.println("\nPress Enter to leave empty.");
        }
        double balance;
        try {
            balance = Double.parseDouble(scanner.nextLine());
            if (balance < 0) {
                System.out.println(cliLog + " cannot be negative.");
                return requestForAmount(scanner, cliLog, canEmpty);
            }
        } catch (NumberFormatException e) {
            if (!canEmpty) {
                System.out.println("Invalid amount. Please enter a number.");
                return requestForAmount(scanner, cliLog, false);
            } else {
                return null;
            }
        }
        return balance;
    }

    private static AccounType requestForType(Scanner scanner, boolean canEmpty) {
        System.out.println("\nSelect account type:");


        for (int i = 0; i < AccounType.values().length; i++) {
            System.out.println(i + 1 + ". " + AccounType.values()[i]);
        }
        System.out.print("Enter choice (1-" + AccounType.values().length + "): ");

        if (canEmpty) {
            System.out.println("Press Enter to leave empty.");
        }

        AccounType type;
        String typeChoice = scanner.nextLine();
        try {
            type = AccounType.values()[Integer.parseInt(typeChoice) - 1];
        } catch (Exception e) {
            if (!canEmpty) {
                System.out.println("Invalid choice. Select a valid account type");
                return requestForType(scanner, false);
            }
            return null;
        }
        return type;
    }

    */
/**
     * Helper method to delete an account
     *//*

    private static void deleteAccount(Scanner scanner) {
        System.out.println("\n===== DELETE ACCOUNT =====");

        //Get account recursively
        AccountInterface account = getAccountId(scanner, " ", false, null);

        try {
            AccountInterface accountInterface = accountService.delete(account);
            System.out.println("\n " + accountInterface + " deleted successfully!");
        } catch (Exception e) {
            System.out.println("\n Error deleting account: " + e.getMessage());
        }
    }

    */
/**
     * Helper method to perform update on account
     *//*

    private static void performAccountUpdate(Scanner scanner) {

        System.out.println("\n===== UPDATE ACCOUNT =====");
        //Get account recursively
        AccountInterface account = getAccountId(scanner, " ", false, null);

        System.out.print("Enter account name or leave empty: ");
        String possibleName = scanner.nextLine();

        //Request type
        AccounType type = requestForType(scanner, true);
        //Request amount recusively
        Double actualAmount = requestForAmount(scanner, "Actual amount", true);

        try {
            accountService.modify(account, type, possibleName, actualAmount);
            System.out.println("Update: " + account + " successful!");
        } catch (Exception e) {
            System.out.println("Error updating the account: " + e.getMessage());
        }
    }

    private static AccountInterface getAccountId(Scanner scanner, String cliLog, boolean canEmpty, AccountInterface accountInterface) {
        // Display available accounts
        userService.displayAllAccount(accountInterface);
        System.out.print("Enter" + cliLog + "account ID: ");
        if (canEmpty) {
            System.out.println("Press Enter to leave empty.");
        }
        int accountId;
        String input = "";
        try {
            input = scanner.nextLine();
            accountId = Integer.parseInt(input) - 1;
            //Check if the ID is different from the From account in Movement flow
            if (accountInterface == currentUser.getAccountList().get(accountId)) {
                throw new Exception("Account used as from account for Movement transaction");
            }
        } catch (Exception e) {
            //If input is empty and canEmpty is true avoid recursive
            if (input.isEmpty() && canEmpty) {
                return null;
            }
            System.out.println("Invalid account ID. Please enter a number.");
            return getAccountId(scanner, cliLog, canEmpty, accountInterface);
        }
        try {
            return currentUser.getAccountList().get(accountId);
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Account not found!");
            return getAccountId(scanner, cliLog, canEmpty, accountInterface);
        }
    }

    private static void showTransactionMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n===== TRANSACTION MENU =====");
            System.out.println("1. View All Transactions");
            System.out.println("2. Add Transaction");
            System.out.println("3. Modify Transaction");
            System.out.println("4. Delete Transaction");
            System.out.println("5. Back to Main Menu");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    transactionService.displayAllTransactions();
                    break;
                case "2":
                    createTransactionCLI(scanner);
                    break;
                case "3":
                    performTransactionUpdate(scanner);
                    break;
                case "4":
                    deleteTransaction(scanner);
                    break;
                case "5":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void createTransactionCLI(Scanner scanner) {
        System.out.println("\n===== ADD TRANSACTION =====");
        //Request for type
        TransactionType type = requestForTransactionType(scanner, false);
        //Request amount
        Double amount = requestForAmount(scanner, "Amount", false);
        //Request category
        String category = requestForCategory(scanner, false);
        //Enter the reason
        System.out.print("Enter reason: ");
        String reason = scanner.nextLine();

        //Get accounts array for Transaction
        AccountInterface[] accounts = getTransactionAccount(scanner, type, false);

        try {
            // Required for service
            transactionService.create(type, amount, category, reason, new Date(), accounts[0], accounts[1]);
            System.out.println("Transaction created successfully!");
        } catch (Exception e) {
            System.out.println("Error creating transaction: " + e.getMessage());
        }
    }


    private static AccountInterface[] getTransactionAccount(Scanner scanner, TransactionType type, boolean canEmpty) {
        AccountInterface from = null, to = null;
        switch (type) {
            case INCOME -> to = getAccountId(scanner, " TO ", canEmpty, null);
            case EXPENSE -> from = getAccountId(scanner, " FROM ", canEmpty, null);
            case MOVEMENT -> {
                from = getAccountId(scanner, " FROM ", canEmpty, null);
                to = getAccountId(scanner, " TO ", canEmpty, from);
            }
        }
        return new AccountInterface[]{to, from};
    }

    private static TransactionType requestForTransactionType(Scanner scanner, boolean canEmpty) {
        System.out.println("\nSelect transaction type:");

        for (int i = 0; i < TransactionType.values().length; i++) {
            System.out.println(i + 1 + ". " + TransactionType.values()[i]);
        }
        System.out.print("Enter choice (1-" + TransactionType.values().length + "): ");

        if (canEmpty) {
            System.out.println("Press Enter to leave empty.");
        }

        TransactionType type;
        String typeChoice = scanner.nextLine();
        try {
            type = TransactionType.values()[Integer.parseInt(typeChoice) - 1];
            if (type.equals(MOVEMENT) && currentUser.getAccountList().size() < 2) {
                System.out.println("\nInvalid choice. Movement Transaction need 2 different accounts!");
                return requestForTransactionType(scanner, false);
            }
        } catch (Exception e) {
            if (!canEmpty) {
                System.out.println("Invalid choice. Select a valid account type!");
                return requestForTransactionType(scanner, false);
            }
            return null;
        }
        return type;
    }


    private static String requestForCategory(Scanner scanner, boolean canEmpty) {
        List<String> sortedCategories = currentUser.getSortedCategories();

        if (sortedCategories.isEmpty()) {
            System.out.println("\n⚠️  No categories found. Please insert a new category:");
        } else {
            System.out.println("\nSelect a category by number or type a new one:");
            for (int i = 0; i < sortedCategories.size(); i++) {
                System.out.printf("  %d. %s\n", i + 1, sortedCategories.get(i));
            }
        }

        if (canEmpty) {
            System.out.println("Press Enter to leave empty.");
        }

        System.out.print("Enter category: ");
        String input = scanner.nextLine().trim();

        if (input.isBlank()) {
            return canEmpty ? null : requestForCategory(scanner, false); // retry if required
        }

        try {
            int index = Integer.parseInt(input);
            if (index >= 1 && index <= sortedCategories.size()) {
                return sortedCategories.get(index - 1); // existing category
            } else {
                System.out.println("❌ Invalid index. Please try again.");
                return requestForCategory(scanner, canEmpty);
            }
        } catch (NumberFormatException e) {
            String newCategory = input.toUpperCase(); // normalize input to uppercase
            if (!currentUser.isCategoryAllowed(newCategory)) {
                currentUser.addCategory(newCategory);
                System.out.println("✅ New category added: " + newCategory);
            }
            return newCategory;
        }
    }

    */
/**
     * Helper method to perform update on account
     *//*

    private static void performTransactionUpdate(Scanner scanner) {

        System.out.println("\n===== UPDATE TRANSACTION =====");
        //Get account recursively
        AbstractTransaction transaction = getTransactionId(scanner, " ");

        //Request type
        //TransactionType type = requestForTransactionType(scanner, true);
        //Request amount recusively
        Double newAmount = requestForAmount(scanner, "Actual amount", true);
        //Request for category
        String newCategory = requestForCategory(scanner, true);
        //Request for reason
        System.out.print("Enter reason: ");
        String reason = scanner.nextLine();

        AccountInterface[] accounts = getTransactionAccount(scanner, transaction.getType(), true);
        try {
            transactionService.modify(transaction, newAmount, newCategory, reason, new Date(), accounts[0], accounts[1]);
            System.out.println("Update: " + transaction + " successful!");
        } catch (
                Exception e) {
            System.out.println("Error updating the transaction: " + e.getMessage());
        }

    }

    private static AbstractTransaction getTransactionId(Scanner scanner, String cliLog) {
        // Display available transactions
        transactionService.displayAllTransactions();

        System.out.print("Enter" + cliLog + "transaction ID: ");
        int transactionId;
        try {
            transactionId = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid transaction ID. Please enter a number.");
            return getTransactionId(scanner, cliLog);
        }
        try {
            return transactionService.getAllTransactionsFlattened().get(transactionId);

        } catch (IndexOutOfBoundsException e) {
            System.out.println("Transaction not found!");
            return getTransactionId(scanner, cliLog);
        }
    }

    */
/**
     * Helper method to delete an account
     *//*

    private static void deleteTransaction(Scanner scanner) {
        System.out.println("\n===== DELETE TRANSACTION =====");

        //Get account recursively
        AbstractTransaction transaction = getTransactionId(scanner, " ");

        try {
            transaction = transactionService.delete(transaction);
            System.out.println("\n " + transaction + " deleted successfully!");
        } catch (Exception e) {
            System.out.println("\n Error deleting transaction: " + e.getMessage());
        }
    }
}
*/
