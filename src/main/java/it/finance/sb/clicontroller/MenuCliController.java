package it.finance.sb.clicontroller;

import it.finance.sb.exception.UserCancelledException;
import it.finance.sb.utility.ConsoleUtils;

/**
 * Interface for application menu controllers.
 */
abstract class MenuCliController {
    /**
     * Displays the menu loop for user interaction.
     *
     * @throws UserCancelledException if user cancels the operation
     */
    abstract void show() throws UserCancelledException;

    /**
     * Generic menu loop for CLI navigation.
     *
     * @param title   the menu title
     * @param options the menu options
     * @param actions the corresponding actions
     */
    public void menuLoop(String title, String[] options, Runnable... actions) throws UserCancelledException {
        while (true) {
            int choice = ConsoleUtils.showMenu(title, false, options);
            if (choice == -1 || choice > actions.length || actions[choice - 1] == null) return;
            actions[choice - 1].run();
        }
    }
}
