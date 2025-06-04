package it.finance.sb.utility;

/**
 * Utility to toggle emojis for CLI output.
 * If ENABLED is false, replaces emojis with ASCII labels for better compatibility.
 */
public class ConsoleEmoji {
    public static final boolean ENABLED = Boolean.parseBoolean(System.getProperty("emoji.enabled", "false"));

    public static final String INFO = ENABLED ? "ℹ️ " : "[INFO] ";
    public static final String SUCCESS = ENABLED ? "✅ " : "[OK] ";
    public static final String WARNING = ENABLED ? "⚠️ " : "[!] ";
    public static final String BACK = ENABLED ? "↩️ " : "[BACK] ";
    public static final String ERROR = ENABLED ? "❌ " : "[ERR] ";
    public static final String HEADER = ENABLED ? "🧠 " : "[HDR] ";
    public static final String SECTION = ENABLED ? "🔷 " : "[*] ";
    public static final String MENU = ENABLED ? "🔹 " : "[MENU] ";
    public static final String INPUT = ENABLED ? "👉 " : "> ";

    public static final String LIST   = ENABLED ? "📋 "  : "";
    public static final String ROW    = ENABLED ? "➤ "  : "- ";
    public static final String MONEY  = ENABLED ? "💰 " : "";
    public static final String CAT    = ENABLED ? "📌 " : "";
    public static final String NOTE   = ENABLED ? "📃 " : "";
    public static final String DATE   = ENABLED ? "📅 " : "";

    /* ── AccountPrinter extras ── */
    public static final String ACC_LIST = ENABLED ? "🏦 " : "";
    public static final String TAG      = ENABLED ? "🏷️ " : "";
    public static final String FOLDER   = ENABLED ? "📂 " : "";

    private ConsoleEmoji() {
        throw new IllegalStateException("Utility class");
    }
}
