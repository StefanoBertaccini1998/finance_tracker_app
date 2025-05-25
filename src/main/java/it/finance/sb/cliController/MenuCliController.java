package it.finance.sb.cliController;

import it.finance.sb.exception.UserCancelledException;

/**
 * Interface for application menu controllers.
 */
public interface MenuCliController {
    /**
     * Displays the menu loop for user interaction.
     *
     * @throws UserCancelledException if user cancels the operation
     */
    void show() throws UserCancelledException;
}
