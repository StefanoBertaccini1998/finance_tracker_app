package it.finance.sb.memento;

import it.finance.sb.model.user.User;

import java.time.LocalDateTime;

public class FinanceStateMemento {
    private final User user;
    private final LocalDateTime timestamp;

    public FinanceStateMemento(User user) {
        this.user = user;
        this.timestamp = LocalDateTime.now();
    }

    public User getUser() {
        return user;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
