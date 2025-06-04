package it.finance.sb.clicontroller;

import it.finance.sb.exception.DataValidationException;
import it.finance.sb.exception.MementoException;
import it.finance.sb.exception.UserCancelledException;
import it.finance.sb.logging.LoggerFactory;
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
 * CLI controller responsible for user-related operations.
 * Handles user login, user creation with password validation,
 * and state persistence using the Memento pattern.
 */
public class UserMenuCliController extends MenuCliController {

    public static final String OPERATION_CANCELLED = " Operation cancelled.";
    public static final String OPERATION_CANCELLED_BY_USER = "Operation cancelled by user.";

    private final UserService userService;
    private final MementoService mementoService;
    private static final Logger logger = LoggerFactory.getSafeLogger(UserMenuCliController.class);

    private User currentUser;

    /**
     * Constructs the UserMenuCliController with required services.
     *
     * @param userService    handles user creation and current user context
     * @param mementoService handles loading and saving of user snapshots
     */
    public UserMenuCliController(UserService userService, MementoService mementoService) {
        this.userService = userService;
        this.mementoService = mementoService;
    }

    @Override
    protected String title() {
        return "User Login";
    }

    @Override
    protected List<MenuItem> menuItems() {
        return List.of(
                new MenuItem("Load existing user", this::loadUser),
                new MenuItem("Create new user", this::createNewUser),
                new MenuItem("Close", () -> {
                    currentUser = null;            // signal “quit” to caller
                    requestClose();                // exits renderLoop()
                })      // no-op triggers loop exit
        );
    }

    @Override
    protected void preMenu() {
        System.out.println(ConsoleStyle.section("Load or Create User"));
    }

    @Override
    protected void postMenu() {
        if (currentUser != null) {
            userService.setCurrentUser(currentUser);
        }
    }


    /**
     * Returns the currently logged-in or created user.
     *
     * @return the active user
     */
    public User getCurrentUser() {
        return currentUser;
    }

    /**
     * Loads a saved user by listing available snapshots and prompting for password.
     */
    private void loadUser() {
        logger.info("Started load user flow");
        try {
            List<String> savedUsers = mementoService.listUsers();

            // No users saved, fallback to user creation
            if (savedUsers.isEmpty()) {
                System.out.println(ConsoleStyle.warning("No saved users found. Let's create a new one."));
                createNewUser();
                return;
            }

            // Let the user select a saved snapshot
            int selected = ConsoleUtils.showMenu("Select a saved user", false, savedUsers.toArray(new String[0]));
            if (selected == -1) {
                System.out.println(ConsoleStyle.back(OPERATION_CANCELLED_BY_USER));
                return;
            }

            // Prompt for password (input hidden)
            String enteredPassword = ConsoleUtils.prompt("Enter your password", true);

            Optional<User> loaded = mementoService.loadUser(savedUsers.get(selected - 1));

            // Check if the user was loaded and validate the password
            if (loaded.isPresent()) {
                logger.info("User loaded correctly in");
                User user = loaded.get();
                if (!user.getPassword().equals(PasswordUtils.hash(enteredPassword))) {
                    System.out.println(ConsoleStyle.error("Incorrect password."));
                    return;
                }
                //Set current user
                currentUser = user;
                requestClose();
                logger.info("Completed load user flow");
                System.out.println(ConsoleStyle.success("User loaded: " + currentUser.getName()));
            } else {
                System.out.println(ConsoleStyle.error("Could not load the selected user. A new one will be created."));
                logger.warning("User cannot be loaded");
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
     * Prompts user for input, validates it, and creates a new user.
     * Ensures password confirmation matches and persists the new user.
     */
    public void createNewUser() {
        System.out.println(ConsoleStyle.menuTitle("Create New User"));
        logger.info("Started create new user flow");
        boolean completed = false;
        while (!completed) {
            try {
                // Collect user details
                String name = ConsoleUtils.prompt("Enter your name", false);
                int age = Integer.parseInt(ConsoleUtils.prompt("Enter your age", false));
                Gender gender = ConsoleUtils.selectEnum(Gender.class, "Select gender", false);

                // Ask for password and confirmation
                String password = ConsoleUtils.prompt("Set a password", true);
                String confirmPassword = ConsoleUtils.prompt("Confirm password", true);

                // Confirm passwords match
                if (!password.equals(confirmPassword)) {
                    System.out.println(ConsoleStyle.error("Passwords do not match."));
                    continue;
                }

                // Create and save the user
                currentUser = userService.create(name, age, gender, PasswordUtils.hash(password));
                mementoService.saveUser(currentUser);
                requestClose();
                System.out.println(ConsoleStyle.success("User created and saved!"));
                logger.info("Completed create new user flow");
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

}
