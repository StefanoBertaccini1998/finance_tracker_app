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

        // Header
        System.out.println(ConsoleStyle.section(
                ConsoleEmoji.ACC_LIST + "All Accounts"));

        // Rows
        accounts.stream()
                .sorted(Comparator.comparing(AccountInterface::getName))
                .forEach(acc -> System.out.printf(
                        "  %s%sName: %-15s | %sBalance: %-10.2f | %sType: %-10s%n",
                        ConsoleEmoji.ROW,
                        ConsoleEmoji.TAG,   acc.getName(),
                        ConsoleEmoji.MONEY, acc.getBalance(),
                        ConsoleEmoji.FOLDER,acc.getType()
                ));
    }
}
