package it.finance.sb.utility;

public class ConsoleStyle {
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\033[1m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    private ConsoleStyle() {
        throw new IllegalStateException("Utility class");
    }

    public static String info(String msg) {
        return CYAN + "\n" + ConsoleEmoji.INFO + msg + RESET;
    }

    public static String success(String msg) {
        return GREEN + "\n" + ConsoleEmoji.SUCCESS + " " + msg + RESET;
    }

    public static String warning(String msg) {
        return YELLOW + "\n" + ConsoleEmoji.WARNING + msg + RESET;
    }

    public static String back(String msg) {
        return YELLOW + "\n" + ConsoleEmoji.BACK + msg + RESET;
    }

    public static String error(String msg) {
        return RED + "\n" + ConsoleEmoji.ERROR + " " + msg + RESET;
    }

    public static String header(String msg) {
        return BOLD + BLUE + "\n" + ConsoleEmoji.HEADER + " " + msg + RESET;
    }

    public static String section(String msg) {
        return BOLD + PURPLE + "\n" + ConsoleEmoji.SECTION + " " + msg + RESET;
    }

    public static String menuTitle(String title) {
        return BOLD + "\n" + ConsoleEmoji.MENU + " " + title.toUpperCase() + RESET;
    }

    public static String inputPrompt(String label) {
        return BOLD + WHITE + ConsoleEmoji.INPUT + " " + label + ": " + RESET;
    }

    public static String highlight(String value) {
        return BOLD + PURPLE + value + RESET;
    }

    public static String actionSuccess(String label, Object target) {
        return success(label + ": ") + CYAN + target + RESET;
    }
}
