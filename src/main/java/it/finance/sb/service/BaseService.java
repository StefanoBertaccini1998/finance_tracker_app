package it.finance.sb.service;

import it.finance.sb.exception.UserLoginException;
import it.finance.sb.model.user.User;

/**
 * Base service class that contains user context
 */
public abstract class BaseService {
    protected User currentUser;

    /**
     * Sets the current user for the service session
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Gets the current user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Checks if a user is logged in
     */
    public boolean isUserLoggedIn() {
        return currentUser != null;
    }

    /**
     * Validates that a user is logged in, throws exception if not
     */
    protected void requireLoggedInUser() throws UserLoginException {
        if (!isUserLoggedIn()) {
            throw new UserLoginException("Operation requires a logged in user");
        }
    }

}