package it.finance.sb.service;

import it.finance.sb.exception.UserLoginException;
import it.finance.sb.model.user.User;

/**
 * BaseService provides core user session control and validation
 * to be extended by all service classes.
 */
public abstract class BaseService {

    /**
     * The currently logged-in user. Shared context for services.
     */
    protected User currentUser;

    /**
     * Sets the current user for the service session.
     *
     * @param user the logged-in user
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    /**
     * Gets the current user.
     *
     * @return the current user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Checks if a user session is active.
     *
     * @return true if logged in, false otherwise
     */
    public boolean isUserLoggedIn() {
        return currentUser != null;
    }

    /**
     * Validates that a user is logged in before allowing sensitive operations.
     *
     * @throws UserLoginException if user is not logged in
     */
    protected void requireLoggedInUser() throws UserLoginException {
        if (!isUserLoggedIn()) {
            throw new UserLoginException("Operation requires a logged-in user.");
        }
    }
}