package it.finance.sb.utility;

import it.finance.sb.model.transaction.AbstractTransaction;

import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;

/**
 * Utility class for printing user transactions.
 */
public class TransactionPrinter {

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private TransactionPrinter() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Displays a sorted, formatted list of transactions.
     *
     * @param transactions the list of transactions to print
     */
    public static void printTransactions(List<AbstractTransaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            System.out.println(ConsoleStyle.warning(" No transactions found."));
            return;
        }

        System.out.println(ConsoleStyle.section("📋 All Transactions"));

        transactions.stream()
                .sorted(Comparator.comparingInt(AbstractTransaction::getTransactionId))
                .forEach(tx -> System.out.printf(
                        "  ➤ 💰 Amount: %-8.2f | 📌 Category: %-12s | 📃 Reason: %-20s | 📅 Date: %-12s | Type: %-9s%n",
                        tx.getAmount(),
                        tx.getCategory(),
                        tx.getReason(),
                        dateFormat.format(tx.getDate()),
                        tx.getType().name()
                ));
    }
}
