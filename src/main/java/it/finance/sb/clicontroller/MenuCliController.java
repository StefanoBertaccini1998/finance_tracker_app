package it.finance.sb.clicontroller;

import it.finance.sb.exception.UserCancelledException;
import it.finance.sb.utility.ConsoleUtils;

import java.util.List;

/**
 * Base class for all CLI menu controllers (Template Method pattern).
 */
abstract class MenuCliController {

    private boolean closeRequested = false;

    /**
     * Entry‑point called by concrete controllers.
     *
     * @throws UserCancelledException if user cancels the operation
     */
    public final void display() throws UserCancelledException {
        closeRequested = false;
        preMenu();
        renderLoop();
        postMenu();
    }

    /* Optional hooks  */
    protected void preMenu() {
    }

    protected void postMenu() {
    }

    /* Must‑implement contracts */
    protected abstract String title();

    protected abstract List<MenuItem> menuItems();

    /**
     * Generic menu loop for CLI navigation.
     * Core loop — never overridden.
     */
    private void renderLoop() throws UserCancelledException {
        while (!closeRequested) {
            int choice = ConsoleUtils.showMenu(
                    title(), false,
                    menuItems().stream().map(MenuItem::label).toArray(String[]::new));

            if (choice == -1) return;
            menuItems().get(choice - 1).action().run();
        }
    }

    protected final void requestClose() {
        closeRequested = true;
    }

}
