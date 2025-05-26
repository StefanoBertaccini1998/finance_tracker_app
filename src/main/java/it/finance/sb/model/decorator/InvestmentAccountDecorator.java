package it.finance.sb.model.decorator;

import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.AccountInterface;

/**
 * Decorator that adds investment functionality to any account
 */
public class InvestmentAccountDecorator implements AccountInterface {
    private final AccountInterface wrapped;
    private double interestRate; // Annual interest rate (e.g., 0.05 for 5%)

    public InvestmentAccountDecorator(AccountInterface wrapped, double interestRate) {
        this.wrapped = wrapped;
        this.interestRate = interestRate;
    }

    /**
     * Projects account growth over time
     * @param years Number of years to project
     * @param additionalMonthlyDeposit Optional monthly deposit amount
     * @return Array of projected balances for each year
     */
    public double[] projectGrowth(int years, double additionalMonthlyDeposit) {
        double[] projections = new double[years + 1];
        projections[0] = getBalance();

        for (int year = 1; year <= years; year++) {
            // Start with previous year's balance
            double yearEndBalance = projections[year - 1];

            // Add monthly deposits across 12 months
            for (int month = 0; month < 12; month++) {
                // Add monthly deposit
                yearEndBalance += additionalMonthlyDeposit;

                // Apply monthly interest (annual rate / 12)
                yearEndBalance += yearEndBalance * (interestRate / 12);
            }

            projections[year] = yearEndBalance;
        }

        return projections;
    }

    /**
     * Returns simple investment summary information
     */
    public String getInvestmentSummary(int projectionYears) {
        double[] projection = projectGrowth(projectionYears, 0);
        double initialAmount = getBalance();
        double finalAmount = projection[projectionYears];
        double totalInterest = finalAmount - initialAmount;

        return String.format("""
                        Investment Summary for %s (Rate: %.2f%%)
                        "
                        "Initial balance: $%.2f
                        "
                        "Projected balance after %d years: $%.2f
                        "
                        "Total interest earned: $%.2f""",
                getName(),
                interestRate * 100,
                initialAmount,
                projectionYears,
                finalAmount,
                totalInterest
        );
    }

    public double getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(double interestRate) {
        this.interestRate = interestRate;
    }

    // Implementation of the AccountInterface methods
    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public double getBalance() {
        return wrapped.getBalance();
    }

    @Override
    public int getAccountId() {
        return wrapped.getAccountId();
    }

    @Override
    public AccounType getType() {
        return wrapped.getType();
    }

    @Override
    public void setType(AccounType type) {
        //Not implemented
    }

    @Override
    public void update(double amount) {
        wrapped.update(amount);
    }

    @Override
    public void setDeposit(double deposit) {
        wrapped.setDeposit(deposit);
    }

    @Override
    public void setName(String name) {
        wrapped.setName(name);
    }
}