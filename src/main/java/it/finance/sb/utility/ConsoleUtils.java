package it.finance.sb.utility;

import java.util.List;
import java.util.Scanner;

public class ConsoleUtils {
    private static final Scanner scanner = new Scanner(System.in);

    public static int showMenu(String title, String... options) {
        System.out.println(ConsoleStyle.menuTitle(title));
        for (int i = 0; i < options.length; i++) {
            System.out.printf("  %d️⃣  %s%n", i + 1, options[i]);
        }
        System.out.print(ConsoleStyle.inputPrompt("Select an option: "));
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println(ConsoleStyle.error("Invalid number. Please try again."));
            return showMenu(title, options);
        }
    }

    public static String prompt(String label, boolean allowEmpty) {
        System.out.print(ConsoleStyle.inputPrompt(label + ": "));
        String input = scanner.nextLine().trim();
        if (!allowEmpty && input.isEmpty()) {
            System.out.println(ConsoleStyle.warning("This field cannot be empty."));
            return prompt(label, false);
        }
        return input;
    }

    public static Double promptForDouble(String label, boolean allowEmpty) {
        String input = prompt(label, allowEmpty);
        if (input.isBlank() && allowEmpty) return null;
        try {
            double val = Double.parseDouble(input);
            if (val < 0) throw new NumberFormatException();
            return val;
        } catch (NumberFormatException e) {
            System.out.println(ConsoleStyle.error("Invalid number. Must be ≥ 0."));
            return promptForDouble(label, allowEmpty);
        }
    }

    public static <E extends Enum<E>> E selectEnum(Class<E> enumClass, String title, boolean allowEmpty) {
        E[] values = enumClass.getEnumConstants();
        System.out.println(ConsoleStyle.menuTitle("Select " + title));
        for (int i = 0; i < values.length; i++) {
            System.out.printf("  %d. %s%n", i + 1, values[i]);
        }

        String input = prompt("Enter choice", allowEmpty);
        if (input.isBlank() && allowEmpty) return null;

        try {
            return values[Integer.parseInt(input) - 1];
        } catch (Exception e) {
            System.out.println(ConsoleStyle.error("Invalid choice. Try again."));
            return selectEnum(enumClass, title, allowEmpty);
        }
    }

    public static String selectOrCreateCategory(List<String> existing, boolean allowEmpty) {
        if (existing.isEmpty()) {
            System.out.println(ConsoleStyle.warning("No categories found. Please type a new one."));
        } else {
            System.out.println(ConsoleStyle.section("Select a category or type a new one:"));
            for (int i = 0; i < existing.size(); i++) {
                System.out.printf("  %d. %s%n", i + 1, existing.get(i));
            }
        }

        String input = prompt("Enter category", allowEmpty);
        if (input.isBlank()) return allowEmpty ? null : selectOrCreateCategory(existing, allowEmpty);

        try {
            int index = Integer.parseInt(input);
            if (index >= 1 && index <= existing.size()) {
                return existing.get(index - 1);
            }
            System.out.println(ConsoleStyle.error("Invalid index."));
            return selectOrCreateCategory(existing, allowEmpty);
        } catch (NumberFormatException e) {
            return input.toUpperCase();
        }
    }
}
