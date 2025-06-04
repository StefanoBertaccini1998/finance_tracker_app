package it.finance.sb.utility;

import it.finance.sb.exception.UserCancelledException;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

public class ConsoleUtils {

    private static final Scanner scanner = new Scanner(System.in);

    private ConsoleUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static int showMenu(String title, String... options) throws UserCancelledException {
        return showMenu(title, true, options);
    }

    public static int showMenu(String title,
                               boolean allowBack,
                               String... options) throws UserCancelledException {
        System.out.println(ConsoleStyle.menuTitle(title));

        // Print numbered options, nicely coloured
        IntStream.range(0, options.length)
                .forEach(i -> System.out.println(
                        ConsoleStyle.info((i + 1) + ". " + options[i])));

        // Interactive loop
        while (true) {
            String input = prompt("Select option", false, allowBack);

            // prompt(…) returns **null** when the user typed “back”
            // (only possible if allowBack == true)
            if (input == null) return -1;

            try {
                int selected = Integer.parseInt(input.trim());
                if (selected >= 1 && selected <= options.length) {
                    return selected;                 // ✅ valid choice
                }
                System.out.println(ConsoleStyle.warning("Option out of range. Try again."));
            } catch (NumberFormatException e) {
                System.out.println(ConsoleStyle.warning("Invalid input. Enter a number."));
            }
        }
    }

    // --- Prompt Strings & Numbers ---

    public static String prompt(String label, boolean allowEmpty) throws UserCancelledException {
        return prompt(label, allowEmpty, true);
    }

    public static String prompt(String label, boolean allowEmpty, boolean allowBack) throws UserCancelledException {
        System.out.print(ConsoleStyle.inputPrompt(
                label + (allowEmpty ? " (leave blank to skip) " : " ")
                        + (allowBack ? "(type 'back' to cancel)" : "")));
        String input = scanner.nextLine().trim();

        if (allowBack && input.equalsIgnoreCase("back")) {
            throw new UserCancelledException();
        }

        if (!allowEmpty && input.isBlank()) {
            System.out.println(ConsoleStyle.warning("Field cannot be empty."));
            return prompt(label, false, allowBack);
        }

        return input;
    }

    public static Double promptForDouble(String label, boolean allowEmpty) throws UserCancelledException {
        return promptForDouble(label, allowEmpty, true);
    }

    /**
     * Prompts the user for a numeric (double) input with optional validation.
     *
     * @param label      the prompt label
     * @param allowEmpty if true, allows empty input (returns null)
     * @param allowBack  if true, allows user to type 'back' to cancel
     * @return parsed Double value or null
     * @throws UserCancelledException if user types 'back'
     */
    public static Double promptForDouble(String label, boolean allowEmpty, boolean allowBack) throws UserCancelledException {
        while (true) {
            String input;
            input = prompt(label, allowEmpty, allowBack);
            boolean isBlank = input == null || input.isBlank();
            if (isBlank && allowEmpty) {
                return null;
            }
            if (isBlank) {
                System.out.println(ConsoleStyle.error("A value is required."));
            } else {
                try {
                    double value = Double.parseDouble(input);
                    if (value < 0) {
                        System.out.println(ConsoleStyle.error("Value must be zero or positive."));
                    } else {
                        return value;
                    }
                } catch (NumberFormatException e) {
                    System.out.println(ConsoleStyle.error("Please enter a valid number."));
                }
            }
        }
    }

    // --- Enum Selection ---
    public static <E extends Enum<E>> E selectEnum(Class<E> enumClass, String title, boolean allowEmpty) throws UserCancelledException {
        return selectEnum(enumClass, title, allowEmpty, true);
    }

    public static <E extends Enum<E>> E selectEnum(Class<E> enumClass, String title, boolean allowEmpty, boolean allowBack) throws UserCancelledException {
        E[] values = enumClass.getEnumConstants();

        while (true) {
            System.out.println(ConsoleStyle.menuTitle("Select " + title));
            IntStream.range(0, values.length)
                    .forEach(i ->
                            System.out.println(ConsoleStyle.info("  " + (i + 1) + ". " + values[i])));

            String input = prompt("Enter choice", allowEmpty, allowBack);

            if (input.isBlank()) {
                if (allowEmpty) return null;
                System.out.println(ConsoleStyle.error("A selection is required."));
                continue;
            }

            try {
                int index = Integer.parseInt(input) - 1;
                if (index >= 0 && index < values.length) {
                    return values[index];
                }
                System.out.println(ConsoleStyle.error("Choice out of range."));
            } catch (NumberFormatException e) {
                System.out.println(ConsoleStyle.error("Invalid number. Try again."));
            }
        }
    }

    // --- Category Input ---

    public static String selectOrCreateCategory(List<String> existing, boolean allowEmpty) throws UserCancelledException {
        return selectOrCreateCategory(existing, allowEmpty, true);
    }

    public static String selectOrCreateCategory(List<String> existing, boolean allowEmpty, boolean allowBack) throws UserCancelledException {
        if (existing.isEmpty()) {
            System.out.println(ConsoleStyle.warning("No categories found. Please type a new one."));
        } else {
            System.out.println(ConsoleStyle.section("Select a category or type a new one:"));
            IntStream.range(0, existing.size())
                    .forEach(i ->
                            System.out.println(ConsoleStyle.info("  " + (i + 1) + ". " + existing.get(i))));
        }

        String input = prompt("Enter category", allowEmpty, allowBack);
        if (input.isBlank()) return allowEmpty ? null : selectOrCreateCategory(existing, false, allowBack);

        try {
            int index = Integer.parseInt(input);
            if (index >= 1 && index <= existing.size()) {
                return existing.get(index - 1);
            }
            System.out.println(ConsoleStyle.error("Invalid index."));
            return selectOrCreateCategory(existing, allowEmpty, allowBack);
        } catch (NumberFormatException e) {
            return input.toUpperCase();
        }
    }



    /**
     * Generic selector that prints a numbered list, prompts for an index,
     * and returns the chosen element.
     *
     * @param list        the items to show (empty → UserCancelledException)
     * @param label       context text such as "account", "transaction"
     * @param nullable    if true the user may type <code>back</code> to cancel
     * @throws UserCancelledException if the user types <code>back</code>
     */
    public static <T> T selectFromList(List<T> list, String label, boolean nullable)
            throws UserCancelledException {
        return selectFromList(list, label, nullable, x -> false, Object::toString);
    }

    /**
     * Generic selector that prints a numbered list, prompts for an index,
     * and returns the chosen element.
     *
     * @param list        the items to show (empty → UserCancelledException)
     * @param label       context text such as "account", "transaction"
     * @param nullable    if true the user may type <code>back</code> to cancel
     * @param skip        optional predicate to hide items (e.g. exclude "from" account);
     *                    pass <code>x -> false</code> if no filtering is needed
     * @param mapper      maps an element to the line to display; pass <code>Object::toString</code>
     *                    for default behaviour
     * @throws UserCancelledException if the user types <code>back</code>
     */
    public static <T> T selectFromList(
            List<T> list,
            String label,
            boolean nullable,
            Predicate<T> skip,
            Function<T, String> mapper) throws UserCancelledException {

        Objects.requireNonNull(skip);
        Objects.requireNonNull(mapper);

        if (list == null || list.isEmpty() || list.stream().allMatch(skip)) {
            throw new NoSuchElementException("No selectable " + label + "s available.");
        }

        // 1️⃣ Build visible index → item map
        Map<Integer, T> visible = new LinkedHashMap<>();
        int index = 1;
        for (T item : list) {
            if (skip.test(item)) continue;
            visible.put(index++, item);
        }

        // 2️⃣ Print nicely
        visible.forEach((i, item) ->
                System.out.println(ConsoleStyle.info(i + ". " + mapper.apply(item))));

        // 3️⃣ Prompt & validate
        while (true) {
            String input = prompt("Enter " + label + " index" +
                    (nullable ? " (or 'back')" : ""), !nullable);

            if (nullable && "back".equalsIgnoreCase(input)) {
                throw new UserCancelledException();
            }
            try {
                int idx = Integer.parseInt(input.trim());
                if (visible.containsKey(idx)) {
                    return visible.get(idx);
                }
                System.out.println(ConsoleStyle.error("Index out of range."));
            } catch (NumberFormatException ex) {
                System.out.println(ConsoleStyle.error("Invalid index."));
            }
        }
    }
}
