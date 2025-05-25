package it.finance.sb.service;

import it.finance.sb.exception.DataValidationException;
import it.finance.sb.exception.UserLoginException;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;
import it.finance.sb.utility.InputSanitizer;

import java.util.Objects;
import java.util.logging.Logger;

/**
 * The type User service.
 */
public class UserService extends BaseService {

    private static final Logger logger = LoggerFactory.getInstance().getLogger(UserService.class);

    /**
     * Create user.
     *
     * @param name   the name
     * @param age    the age
     * @param gender the gender
     * @return the user
     */
    public User create(String name, int age, Gender gender) throws DataValidationException {
        if (name == null || name.isBlank() || gender == null || age < 1) {
            throw new DataValidationException("Invalid input for user creation.");
        }

        User user = new User(name.trim(), age, gender);
        InputSanitizer.validate(user);
        setCurrentUser(user);

        logger.info(() -> String.format("[UserService] Created new user: name='%s', age=%d", user.getName(), age));
        return user;
    }

    /**
     * Delete user.
     *
     * @param user the user
     * @return the user
     */
    public User delete(User user) {
        Objects.requireNonNull(user, "User cannot be null");
        logger.info(() -> "[UserService] Deleted user: " + user.getName());
        return user;
    }

    /**
     * Modify user.
     *
     * @param user      the user
     * @param newName   the new name
     * @param newAge    the new age
     * @param newGender the new gender
     * @return the user
     */
    public User modify(User user, String newName, Integer newAge, Gender newGender) throws DataValidationException {
        Objects.requireNonNull(user, "User cannot be null");

        if (newName != null && !newName.trim().isEmpty()) {
            user.setName(newName.trim());
        }
        if (newAge != null && newAge > 0) {
            user.setAge(newAge);
        }
        if (newGender != null) {
            user.setGender(newGender);
        }

        InputSanitizer.validate(user);
        logger.info(() -> String.format("[UserService] Modified user: name='%s', age=%d", user.getName(), user.getAge()));
        return user;
    }

    /**
     * Safely retrieves the current user or throws.
     */
    public User getVerifiedUser() throws UserLoginException {
        User user = getCurrentUser();
        if (user == null) {
            throw new UserLoginException("No user is logged in.");
        }
        return user;
    }


    /**
     * Add category.
     *
     * @param category the category
     */
    public void addCategory(String category) throws UserLoginException {
        requireLoggedInUser();
        if (category == null || category.isBlank()) {
            logger.warning("[UserService] Rejected blank category.");
            return;
        }
        if (!currentUser.isCategoryAllowed(category)) {
            currentUser.addCategory(category);
            logger.info("[UserService] Added category: '" + category + "'");
        }
    }

}
