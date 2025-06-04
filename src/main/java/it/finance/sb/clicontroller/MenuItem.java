package it.finance.sb.clicontroller;

import java.util.Objects;

public record MenuItem(String label, Runnable action) {
    public MenuItem {
        Objects.requireNonNull(label, "label must not be null");
        Objects.requireNonNull(action, "action must not be null");
    }
}
