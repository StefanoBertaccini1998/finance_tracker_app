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
     * @param txs the list of transactions to print
     */
    public static void printTransactions(List<AbstractTransaction> txs) {
        if (txs == null || txs.isEmpty()) {
            System.out.println(ConsoleStyle.warning(" No transactions found."));
            return;
        }

        // Header ───────────────────────────────────────
        System.out.println(ConsoleStyle.section(
                ConsoleEmoji.LIST + "All Transactions"));

        // Body rows ────────────────────────────────────
        txs.stream()
                .sorted(Comparator.comparingInt(AbstractTransaction::getTransactionId))
                .forEach(tx -> System.out.printf(
                        "  %s%sAmount: %-8.2f | %sCategory: %-12s | %sReason: %-20s | %sDate: %-10s | Type: %-9s%n",
                        ConsoleEmoji.ROW,
                        ConsoleEmoji.MONEY,      tx.getAmount(),
                        ConsoleEmoji.CAT,        tx.getCategory(),
                        ConsoleEmoji.NOTE,       tx.getReason(),
                        ConsoleEmoji.DATE,       dateFormat.format(tx.getDate()),
                        tx.getType().name()
                ));
    }
}