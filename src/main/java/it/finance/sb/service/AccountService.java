package it.finance.sb.service;

import it.finance.sb.exception.AccountOperationException;
import it.finance.sb.exception.DataValidationException;
import it.finance.sb.exception.UserLoginException;
import it.finance.sb.factory.AccountFactory;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.utility.ConsoleStyle;
import it.finance.sb.utility.InputSanitizer;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The type Account service.
 */
public class AccountService extends BaseService {

    private final TransactionService transactionService;
    private static final Logger logger = LoggerFactory.getInstance().getLogger(AccountService.class);

    /**
     * Instantiates a new Account service.
     *
     * @param transactionService the transaction service
     */
    public AccountService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * Create abstract account.
     *
     * @param type    the type
     * @param name    the name
     * @param balance the balance
     * @return the abstract account
     * @throws AccountOperationException,DataValidationException,UserLoginException the exception
     */
    public AccountInterface create(AccounType type, String name, Double balance) throws AccountOperationException, DataValidationException, UserLoginException {
        requireLoggedInUser();

        try {
            if (type == null || name == null || name.isBlank() || balance == null || balance < 0) {
                throw new AccountOperationException("Invalid input for account creation.");
            }
            //Create account using factory
            AccountInterface accountCreated = AccountFactory.createAccount(type, name, balance);
            //Sanitize the object
            InputSanitizer.validate(accountCreated);
            //Add to the current user context the account created
            currentUser.addAccount(accountCreated);
            logger.info(() -> String.format("[AccountService] Account created for user='%s' (type=%s)",
                    currentUser.getName(), type));
            return accountCreated;
        } catch (DataValidationException | AccountOperationException e) {
            logger.warning("[AccountService] Account creation failed: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error in account creation", e);
            throw new AccountOperationException("Unexpected error during account creation.", e);
        }
    }

    /**
     * Delete abstract account.
     *
     * @param account the account
     * @return the abstract account
     */
    public AccountInterface delete(AccountInterface account) throws AccountOperationException, UserLoginException {
        requireLoggedInUser();
        if (account == null) {
            throw new AccountOperationException("Cannot delete a null account.");
        }
        try {
            // Remove all transactions linked to this account
            transactionService.removeTransactionsForAccount(account);

            // Remove account from user
            currentUser.removeAccount(account);
            logger.info(() -> String.format("[AccountService] Account deleted (ID=%d)", account.getAccountId()));
            return account;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error deleting account", e);
            throw new AccountOperationException("Failed to delete account.", e);

        }
    }

    /**
     * Modify abstract account.
     *
     * @param account    the account
     * @param type       the account type
     * @param newName    the new name
     * @param newBalance the new balance
     * @return the abstract account
     */
    public AccountInterface modify(AccountInterface account, AccounType type, String newName, Double newBalance) throws DataValidationException, UserLoginException, AccountOperationException {
        requireLoggedInUser();
        if (account == null) {
            throw new AccountOperationException("Cannot modify a null account.");
        }
        try {
            if (newName != null && !newName.trim().isEmpty()) {
                account.setName(newName);
            }
            if (type != null) {
                account.setType(type);
            }
            if (newBalance != null) {
                if (newBalance < 0) {
                    throw new AccountOperationException("Account balance cannot be negative.");
                }
                account.setDeposit(newBalance);
            }
            InputSanitizer.validate(account);
            logger.info(() -> String.format("[AccountService] Account modified (ID=%d)", account.getAccountId()));
            return account;
        } catch (DataValidationException | AccountOperationException e) {
            logger.warning("[AccountService] Modification failed: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error modifying account", e);
            throw new AccountOperationException("Unexpected error during account modification.", e);
        }
    }

    /**
     * Display all account balances without the account used for movement
     */
    public void displayAllAccount(AccountInterface accountInterfaceAvoid) {
        if (currentUser.getAccountList().isEmpty()) {
            System.out.println(ConsoleStyle.warning(" No accounts found."));
        } else {
            logger.info("[UserService] Showing all balances for user '" + getCurrentUser().getName() + "'");
            List<AccountInterface> accountList = getCurrentUser().getAccountList();
            for (int i = 0; i < accountList.size(); i++) {
                if (accountInterfaceAvoid == null || accountInterfaceAvoid != accountList.get(i)) {
                    AccountInterface accountInterface = accountList.get(i);
                    System.out.println(i + 1 + ") " + accountInterface);
                }
            }
        }
    }


    /**
     * Display all account balances.
     */
    public void displayAllAccount() {
        displayAllAccount(null);
    }


}
