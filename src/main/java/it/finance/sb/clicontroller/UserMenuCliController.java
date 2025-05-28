package it.finance.sb.clicontroller;

import it.finance.sb.exception.DataValidationException;
import it.finance.sb.exception.MementoException;
import it.finance.sb.exception.UserCancelledException;
import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;
import it.finance.sb.service.MementoService;
import it.finance.sb.service.UserService;
import it.finance.sb.utility.ConsoleStyle;
import it.finance.sb.utility.ConsoleUtils;
import it.finance.sb.utility.PasswordUtils;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * CLI controller responsible for user login, creation, and saving operations.
 * <p>
 * Provides a text-based interface to load existing users, create new users,
 * and persist user data using the Memento pattern.
 * </p>
 */
public class UserMenuCliController implements MenuCliController {

    public static final String OPERATION_CANCELLED = " Operation cancelled.";
    public static final String OPERATION_CANCELLED_BY_USER = "Operation cancelled by user.";
    private final UserService userService;
    private final MementoService mementoService;
    private final Logger logger;

    private User currentUser;

    /**
     * Constructs the UserMenuCliController with the required services.
     *
     * @param userService    the user creation and authentication service
     * @param mementoService the user snapshot (persistence) manager
     * @param logger         the logging facility
     */
    public UserMenuCliController(UserService userService, MementoService mementoService, Logger logger) {
        this.userService = userService;
        this.mementoService = mementoService;
        this.logger = logger;
    }

    /**
     * Displays the user login menu with options to load or create a user.
     *
     * @throws UserCancelledException if the user exits the menu
     */
    @Override
    public void show() throws UserCancelledException {
        System.out.println(ConsoleStyle.section("🔐 Load or Create User"));

        while (currentUser == null) {
            int choice = ConsoleUtils.showMenu(
                    "User Login",
                    false,
                    "Load existing user",
                    "Create new user"
            );

            if (choice == -1) {
                System.out.println(ConsoleStyle.back(OPERATION_CANCELLED));
                throw new UserCancelledException(); // exit entire flow if user cancels
            }

            switch (choice) {
                case 1 -> loadUser();
                case 2 -> createNewUser();
                default -> System.out.println(ConsoleStyle.warning(" Invalid option selected."));
            }
        }

        userService.setCurrentUser(currentUser);
    }

    /**
     * Returns the currently loaded or created user.
     *
     * @return the active {@link User}
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Loads a user from saved snapshots.
     */
    private void loadUser() {
        try {
            List<String> savedUsers = mementoService.listUsers();

            if (savedUsers.isEmpty()) {
                System.out.println(ConsoleStyle.warning("No saved users found. Let's create a new one."));
                createNewUser();
                return;
            }

            int selected = ConsoleUtils.showMenu("Select a saved user", savedUsers.toArray(new String[0]));
            if (selected == -1) {
                System.out.println(ConsoleStyle.back(OPERATION_CANCELLED_BY_USER));
                return;
            }

            String enteredPassword = ConsoleUtils.prompt("Enter your password", true); // true = hide input

            Optional<User> loaded = mementoService.loadUser(savedUsers.get(selected - 1));

            if (loaded.isPresent()) {
                User user = loaded.get();
                if (!user.getPassword().equals(PasswordUtils.hash(enteredPassword))) {
                    System.out.println(ConsoleStyle.error("Incorrect password."));
                    return;
                }
                currentUser = user;
                System.out.println(ConsoleStyle.success("User loaded: " + currentUser.getName()));
            } else {
                System.out.println(ConsoleStyle.error("Could not load the selected user. A new one will be created."));
                createNewUser();
            }

        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(OPERATION_CANCELLED_BY_USER));
        } catch (MementoException e) {
            logger.log(Level.SEVERE, "Failed to load user snapshot", e);
            System.out.println(ConsoleStyle.error("Failed to load user data. The file might be missing or corrupted."));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error while loading user", e);
            System.out.println(ConsoleStyle.error("An unexpected error occurred while loading user data."));
        }
    }

    /**
     * Creates a new user by prompting the user for input.
     */
    public void createNewUser() {
        System.out.println(ConsoleStyle.menuTitle("Create New User"));

        boolean completed = false;
        while (!completed) {
            try {
                String name = ConsoleUtils.prompt("Enter your name", false);

                int age = Integer.parseInt(ConsoleUtils.prompt("Enter your age", false));

                Gender gender = ConsoleUtils.selectEnum(Gender.class, "Select gender", false);

                String password = ConsoleUtils.prompt("Set a password", true);
                String confirmPassword = ConsoleUtils.prompt("Confirm password", true);

                if (!password.equals(confirmPassword)) {
                    System.out.println(ConsoleStyle.error("Passwords do not match."));
                    continue;
                }

                currentUser = userService.create(name, age, gender, PasswordUtils.hash(password));

                mementoService.saveUser(currentUser);

                System.out.println(ConsoleStyle.success("User created and saved!"));
                completed = true;
            } catch (UserCancelledException e) {
                System.out.println(ConsoleStyle.back(OPERATION_CANCELLED_BY_USER));
                break;
            } catch (NumberFormatException e) {
                logger.warning("Invalid age input: " + e.getMessage());
                System.out.println(ConsoleStyle.error("Please enter a valid number for age."));
            } catch (DataValidationException e) {
                logger.warning("Invalid user data: " + e.getMessage());
                System.out.println(ConsoleStyle.error("Invalid input: " + e.getMessage()));
            } catch (MementoException e) {
                logger.severe("Failed to save user data: " + e.getMessage());
                System.out.println(ConsoleStyle.error("Could not save user data. Check disk permissions or retry."));
            } catch (Exception e) {
                logger.severe("Unexpected error during user creation: " + e.getMessage());
                System.out.println(ConsoleStyle.error("An unexpected error occurred. Please try again."));
            }
        }
    }

    /**
     * Saves the current user state to disk using the memento service.
     */
    public void saveUser() {
        try {
            mementoService.saveUser(currentUser);
            System.out.println(ConsoleStyle.success(" User saved."));
        } catch (MementoException e) {
            logger.log(Level.SEVERE, "Failed to save user", e);
            System.out.println(ConsoleStyle.error(" Failed to save user."));
        }
    }

}
