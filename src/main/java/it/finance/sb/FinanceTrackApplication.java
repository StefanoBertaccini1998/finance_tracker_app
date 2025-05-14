package it.finance.sb;

import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.TransactionType;
import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;
import it.finance.sb.service.AccountService;
import it.finance.sb.service.InvestmentService;
import it.finance.sb.service.TransactionService;
import it.finance.sb.service.UserService;

import java.util.Date;
import java.util.Scanner;

public class FinanceTrackApplication {
    private static User currentUser;
    private static final TransactionService transactionService = new TransactionService();
    private static final UserService userService = new UserService();
    private static final AccountService accountService = new AccountService(transactionService);
    private static final InvestmentService investmentService = new InvestmentService(accountService, userService);


    public static void main(String[] args) {
        System.out.println("Welcome to Simple Banking System!");
        // For simplicity, we'll create a default user
        // In a real app, this would have proper authentication
        loginUser();

        // Start the main menu
        showMainMenu();

        System.out.println("Thank you for using Simple Banking System!");
    }


    /**
     * Create and log in a demo user
     */
    private static void loginUser() {
        currentUser = new User("Stefano", 29, Gender.OTHER);
        System.out.println("Logged in as: " + currentUser.getName());

        // Set the user in our services
        userService.setCurrentUser(currentUser);
        accountService.setCurrentUser(currentUser);
        investmentService.setCurrentUser(currentUser);
        transactionService.setCurrentUser(currentUser);
    }

    /**
     * Shows the main application menu
     */
    private static void showMainMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n===== MAIN MENU =====");
            System.out.println("1. Manage Accounts");
            System.out.println("2. Manage Transactions");
            System.out.println("3. Investment Calculator");
            System.out.println("4. Exit");
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

    /**
     * Shows the account management menu
     */
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

    /**
     * Helper method to create a new account
     */
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

    /**
     * Helper method to recursive request a valid amount
     */
    private static Double requestForAmount(Scanner scanner, String cliLog, boolean canEmpty) {
        System.out.print("\nEnter " + cliLog + ":");
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
        if(canEmpty){
            System.out.println("\nSelect account type or leave empty:");
        }else{
            System.out.println("\nSelect account type:");
        }

        for (int i = 0; i < AccounType.values().length; i++) {
            System.out.println(i + 1 + ". " + AccounType.values()[i]);
        }
        System.out.print("Enter choice (1-" + AccounType.values().length + "): ");

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

    /**
     * Helper method to delete an account
     */
    private static void deleteAccount(Scanner scanner) {
        System.out.println("\n===== DELETE ACCOUNT =====");

        //Get account recursively
        AccountInterface account = getAccountId(scanner);

        try {
            AccountInterface accountInterface = accountService.delete(account);
            System.out.println("\n " + accountInterface + " deleted successfully!");
        } catch (Exception e) {
            System.out.println("\n Error deleting account: " + e.getMessage());
        }
    }

    /**
     * Helper method to perform deposits and withdrawals
     */
    //TODO create the method to perform transaction
    /*private static void performAccountTransaction(Scanner scanner, boolean isDeposit) {
        String operation = isDeposit ? TransactionType.INCOME : TransactionType.EXPENSE;

        if (currentUser.getAccountList().isEmpty()) {
            System.out.println("You need to create an account first!");
            return;
        }

        System.out.println("\n===== " + operation.toUpperCase() + " FUNDS =====");

        // Display available accounts
        userService.displayAllAccount();

        System.out.print("Enter account ID: ");
        int accountId;
        try {
            accountId = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid account ID. Please enter a number.");
            return;
        }

        if (currentUser.getAccountList().get(accountId) == null) {
            System.out.println("Account not found!");
            return;
        }

        System.out.print("Enter amount to " + operation.toLowerCase() + ": ");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine());
            if (amount <= 0) {
                System.out.println("Amount must be positive.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount. Please enter a number.");
            return;
        }

        try {
            // For deposits, amount is positive; for withdrawals, amount is negative
            double transactionAmount = isDeposit ? amount : -amount;
            //
            transactionService.create()
            System.out.println(operation + " successful!");
        } catch (Exception e) {
            System.out.println("Error processing " + operation.toLowerCase() + ": " + e.getMessage());
        }
    }*/

    /**
     * Helper method to perform update on account
     */
    private static void performAccountUpdate(Scanner scanner) {

        System.out.println("\n===== UPDATE ACCOUNT =====");
        //Get account recursively
        AccountInterface account = getAccountId(scanner);

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

    private static AccountInterface getAccountId(Scanner scanner) {
        // Display available accounts
        userService.displayAllAccount();

        System.out.print("Enter account ID: ");
        int accountId;
        try {
            accountId = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid account ID. Please enter a number.");
            return getAccountId(scanner);
        }
        try {
            return currentUser.getAccountList().get(accountId);

        } catch (IndexOutOfBoundsException e) {
            System.out.println("Account not found!");
            return getAccountId(scanner);
        }

    }

    //TODO add check if account exist
    private static void showTransactionMenu() {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running) {
            System.out.println("\n===== TRANSACTION MENU =====");
            System.out.println("1. Add Transaction");
            System.out.println("2. View All Transactions");
            System.out.println("3. Back to Main Menu");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    createTransactionCLI(scanner);
                    break;
                case "2":
                    userService.displayAllTransactions();
                    break;
                case "3":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void createTransactionCLI(Scanner scanner) {
        System.out.println("\n===== ADD TRANSACTION =====");

        System.out.println("Select type:");
        System.out.println("1. INCOME");
        System.out.println("2. EXPENSE");
        System.out.println("3. MOVEMENT");

        TransactionType type = null;
        while (type == null) {
            System.out.println("Select transaction type:");
            System.out.println("1. INCOME");
            System.out.println("2. EXPENSE");
            System.out.println("3. MOVEMENT");
            String input = scanner.nextLine();

            switch (input) {
                case "1" -> type = TransactionType.INCOME;
                case "2" -> type = TransactionType.EXPENSE;
                case "3" -> type = TransactionType.MOVEMENT;
                default -> System.out.println("Invalid choice. Please enter 1, 2, or 3.");
            }
        }

        System.out.print("Enter amount: ");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine());
            if (amount <= 0) {
                System.out.println("Amount must be positive.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
            return;
        }

        System.out.print("Enter category: ");
        String category = scanner.nextLine();

        System.out.print("Enter reason: ");
        String reason = scanner.nextLine();

        AccountInterface from = null, to = null;

        switch (type) {
            case INCOME -> {
                System.out.print("Enter TO Account ID: ");
                int toId = Integer.parseInt(scanner.nextLine()) - 1;
                to = currentUser.getAccountList().get(toId);
                if (to == null) {
                    System.out.println("Invalid account.");
                    return;
                }
            }
            case EXPENSE -> {
                System.out.print("Enter FROM Account ID: ");
                int fromId = Integer.parseInt(scanner.nextLine()) - 1;
                from = currentUser.getAccountList().get(fromId);
                if (from == null) {
                    System.out.println("Invalid account.");
                    return;
                }
            }
            case MOVEMENT -> {
                System.out.print("Enter FROM Account ID: ");
                int fromId = Integer.parseInt(scanner.nextLine()) - 1;
                from = currentUser.getAccountList().get(fromId);

                System.out.print("Enter TO Account ID: ");
                int toId = Integer.parseInt(scanner.nextLine()) - 1;
                to = currentUser.getAccountList().get(toId);

                if (from == null || to == null) {
                    System.out.println("Invalid account(s).");
                    return;
                }
            }
        }

        try {
            transactionService.setCurrentUser(currentUser); // ✅ Required for service
            transactionService.create(type, amount, category, reason, new Date(), to, from);
            if (!currentUser.isCategoryAllowed(category)) {
                currentUser.addCategory(category); // Auto add new categories
            }

            System.out.println("Transaction created successfully!");
        } catch (Exception e) {
            System.out.println("Error creating transaction: " + e.getMessage());
        }
    }

}
