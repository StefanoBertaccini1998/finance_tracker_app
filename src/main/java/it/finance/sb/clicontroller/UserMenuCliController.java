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

        menuLoop("User Login",
                new String[]{"Load existing user", "Create new user"},
                this::loadUser,
                this::createNewUser
        );

        // once loaded/created, propagate to services
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
     *
     */
    private void loadUser() {
        try {
            List<String> savedUsers = mementoService.listUsers();

            if (savedUsers.isEmpty()) {
                System.out.println(ConsoleStyle.warning(" No saved users found."));
                createNewUser();
                return;
            }

            int selected = ConsoleUtils.showMenu("Select a saved user", savedUsers.toArray(new String[0]));
            if (selected == -1) {
                System.out.println(ConsoleStyle.back(OPERATION_CANCELLED));
                return;
            }

            Optional<User> loaded = mementoService.loadUser(savedUsers.get(selected - 1));
            if (loaded.isPresent()) {
                currentUser = loaded.get();
                System.out.println(ConsoleStyle.success(" Loaded user: " + currentUser.getName()));
            } else {
                System.out.println(ConsoleStyle.error(" Could not load selected user. Creating a new one."));
                createNewUser();
            }

        } catch (MementoException e) {
            logger.log(Level.SEVERE, "Failed to load user", e);
            System.out.println(ConsoleStyle.error(" Failed to load user."));
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(OPERATION_CANCELLED));
        }
    }

    /**
     * Creates a new user by prompting the user for input.
     */
    public void createNewUser() {
        try {
            String name = ConsoleUtils.prompt("Enter your name", false);
            if (name == null) throw new UserCancelledException();

            String ageStr = ConsoleUtils.prompt("Enter your age", false);
            if (ageStr == null) throw new UserCancelledException();
            int age = Integer.parseInt(ageStr);

            Gender gender = ConsoleUtils.selectEnum(Gender.class, "Select gender", false);
            if (gender == null) throw new UserCancelledException();

            currentUser = userService.create(name, age, gender);
            mementoService.saveUser(currentUser);

            System.out.println(ConsoleStyle.success(" User created and saved!"));

        } catch (DataValidationException e) {
            logger.warning("User data invalid: " + e.getMessage());
            System.out.println(ConsoleStyle.error(" Invalid user data: " + e.getMessage()));
            createNewUser();
        } catch (NumberFormatException e) {
            logger.warning("Invalid age: " + e.getMessage());
            System.out.println(ConsoleStyle.error(" Invalid age format."));
            createNewUser();
        } catch (MementoException e) {
            logger.severe("Failed to save user: " + e.getMessage());
            System.out.println(ConsoleStyle.error(" Could not save user."));
            createNewUser();
        } catch (UserCancelledException e) {
            System.out.println(ConsoleStyle.back(OPERATION_CANCELLED));
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

    /**
     * Displays a generic menu and handles user selection.
     *
     * @param title   the menu title
     * @param options the menu options
     * @param actions the corresponding actions
     * @throws UserCancelledException if user exits menu
     */
    private void menuLoop(String title, String[] options, Runnable... actions) throws UserCancelledException {
        boolean running = true;
        while (running) {
            int choice = ConsoleUtils.showMenu(title, false, options);
            if (choice == -1) return;
            if (choice > actions.length || actions[choice - 1] == null) {
                running = false;
            } else {
                actions[choice - 1].run();
                running = false; // exit after one operation (login complete)
            }
        }
    }
}
