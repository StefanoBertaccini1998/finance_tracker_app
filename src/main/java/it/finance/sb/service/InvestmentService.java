package it.finance.sb.service;

import it.finance.sb.exception.UserLoginException;
import it.finance.sb.model.decorator.InvestmentAccountDecorator;
import it.finance.sb.model.account.AccountInterface;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Service for investment functionality in a Java SE application
 */
public class InvestmentService extends BaseService {

    // Cache for decorated investment accounts
    private final Map<Integer, InvestmentAccountDecorator> investmentAccountCache = new HashMap<>();

    // Reference to the account service
    private final AccountService accountService;

    private final UserService userService;

    /**
     * Constructor with injected account service
     */
    public InvestmentService(AccountService accountService,UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    /**
     * Gets or creates an investment decorator for the given account ID
     */
    public InvestmentAccountDecorator getInvestmentAccount(int accountId, double interestRate) throws UserLoginException {
        requireLoggedInUser();

        // Make sure the account service has the same user context
        accountService.setCurrentUser(getCurrentUser());

        // Check if we already have a decorator for this account
        if (investmentAccountCache.containsKey(accountId)) {
            InvestmentAccountDecorator existing = investmentAccountCache.get(accountId);
            // Update interest rate if it changed
            if (existing.getInterestRate() != interestRate) {
                existing.setInterestRate(interestRate);
            }
            return existing;
        }

        // Get the base account from the account service
        AccountInterface baseAccount = accountService.getCurrentUser().getAccountList().get(accountId);
        if (baseAccount == null) {
            throw new IllegalArgumentException("Account with ID " + accountId + " not found");
        }

        // Create a new investment decorator
        InvestmentAccountDecorator investmentAccount = new InvestmentAccountDecorator(baseAccount, interestRate);

        // Cache it for future use
        investmentAccountCache.put(accountId, investmentAccount);

        return investmentAccount;
    }

    /**
     * Displays an investment menu for the CLI
     */
    public void showInvestmentMenu() throws UserLoginException {
        requireLoggedInUser();
        accountService.setCurrentUser(getCurrentUser());

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n===== INVESTMENT MENU =====");
            System.out.println("1. Project Investment Growth");
            System.out.println("2. Return to Main Menu");
            System.out.print("Select an option: ");

            String choice = scanner.nextLine();

            if (choice.equals("1")) {
                // First, show the user's accounts
                userService.displayAllAccount();

                if (currentUser.getAccountList().isEmpty()) {
                    System.out.println("You need to create an account first!");
                    continue;
                }

                // Get account ID
                System.out.print("Enter the account ID for projection: ");
                int accountId;
                try {
                    accountId = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid account ID. Please enter a number.");
                    continue;
                }

                // Validate the account exists
                AccountInterface account = getCurrentUser().getAccountList().get(accountId);
                if (account == null) {
                    System.out.println("Account not found!");
                    continue;
                }

                // Get interest rate
                System.out.print("Enter annual interest rate (e.g., 0.05 for 5%): ");
                double interestRate;
                try {
                    interestRate = Double.parseDouble(scanner.nextLine());
                    if (interestRate <= 0 || interestRate > 0.5) {
                        System.out.println("Interest rate should be between 0 and 0.5 (0% to 50%)");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid interest rate. Please enter a number.");
                    continue;
                }

                // Get projection years
                System.out.print("Enter number of years for projection: ");
                int years;
                try {
                    years = Integer.parseInt(scanner.nextLine());
                    if (years <= 0 || years > 100) {
                        System.out.println("Years should be between 1 and 100");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number of years. Please enter a number.");
                    continue;
                }

                // Get monthly deposit
                System.out.print("Enter monthly additional deposit (0 for none): ");
                double monthlyDeposit;
                try {
                    monthlyDeposit = Double.parseDouble(scanner.nextLine());
                    if (monthlyDeposit < 0) {
                        System.out.println("Monthly deposit cannot be negative");
                        continue;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid monthly deposit. Please enter a number.");
                    continue;
                }

                // Generate and display projection
                try {
                    String report = generateProjectionReport(accountId, interestRate, years, monthlyDeposit);
                    System.out.println("\n" + report);
                } catch (Exception e) {
                    System.out.println("Error generating projection: " + e.getMessage());
                }

                System.out.println("\nPress Enter to continue...");
                scanner.nextLine();

            } else if (choice.equals("2")) {
                return; // Return to main menu
            } else {
                System.out.println("Invalid option. Please try again.");
            }
        }
    }

    /**
     * Generate a projection report
     */
    private String generateProjectionReport(int accountId, double interestRate, int years, double monthlyDeposit) throws UserLoginException {
        InvestmentAccountDecorator investmentAccount = getInvestmentAccount(accountId, interestRate);

        double[] projections = investmentAccount.projectGrowth(years, monthlyDeposit);
        StringBuilder report = new StringBuilder();

        report.append(String.format("Investment Projection for %s (Account #%d)\n",
                investmentAccount.getName(), accountId));
        report.append(String.format("Annual Interest Rate: %.2f%%\n", interestRate * 100));
        report.append(String.format("Monthly Additional Deposit: $%.2f\n\n", monthlyDeposit));
        report.append("Year\tBalance\t\tInterest Earned\n");
        report.append("----\t-------\t\t--------------\n");

        double initialBalance = projections[0];
        double totalInterestEarned = 0;

        for (int year = 0; year <= years; year++) {
            double yearEndBalance = projections[year];
            double yearlyInterest = 0;

            if (year > 0) {
                // Calculate this year's interest by subtracting previous balance and deposits
                double previousBalance = projections[year - 1];
                double yearlyDeposits = monthlyDeposit * 12;
                yearlyInterest = yearEndBalance - previousBalance - yearlyDeposits;
                totalInterestEarned += yearlyInterest;
            }

            if (year == 0 || year == years || year % 5 == 0) {
                report.append(String.format("%d\t$%.2f\t$%.2f\n",
                        year, yearEndBalance, yearlyInterest));
            }
        }

        report.append("\nSummary:\n");
        report.append(String.format("Initial Balance: $%.2f\n", initialBalance));
        report.append(String.format("Final Balance after %d years: $%.2f\n", years, projections[years]));
        report.append(String.format("Total Deposits: $%.2f\n", monthlyDeposit * 12 * years));
        report.append(String.format("Total Interest Earned: $%.2f\n", totalInterestEarned));

        return report.toString();
    }
}