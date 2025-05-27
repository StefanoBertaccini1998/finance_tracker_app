package it.finance.sb.utility;

import it.finance.sb.model.account.AccountInterface;

import java.util.Comparator;
import java.util.List;

public class AccountPrinter {

    private AccountPrinter() {
        throw new IllegalStateException("Utility class");
    }
    public static void printAccounts(List<AccountInterface> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            System.out.println(ConsoleStyle.warning("No accounts found."));
            return;
        }

        System.out.println(ConsoleStyle.section("🏦 All Accounts"));

        accounts.stream()
                .sorted(Comparator.comparing(AccountInterface::getName)) // ordinamento per nome
                .forEach(acc -> System.out.printf(
                        "  ➤ 🏷️ Name: %-15s | 💰 Balance: %-10.2f | 📂 Type: %-10s%n",
                        acc.getName(),
                        acc.getBalance(),
                        acc.getType()
                ));
    }

    public static void printAccountsExcluding(List<AccountInterface> accounts, AccountInterface exclude) {
        printAccounts(
                accounts.stream()
                        .filter(acc -> !acc.equals(exclude))
                        .toList()
        );
    }
}
