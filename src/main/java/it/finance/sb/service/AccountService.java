package it.finance.sb.service;

import it.finance.sb.exception.AccountOperationException;
import it.finance.sb.exception.DataValidationException;
import it.finance.sb.exception.UserLoginException;
import it.finance.sb.factory.FinanceAbstractFactory;
import it.finance.sb.logging.LoggerFactory;
import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.utility.InputSanitizer;

import java.util.List;
import java.util.logging.Logger;

/**
 * AccountService handles the creation, modification, deletion,
 * and display logic for user accounts.
 */
public class AccountService extends BaseService {

    private final TransactionService transactionService;
    private static final Logger logger = LoggerFactory.getSafeLogger(AccountService.class);
    private final FinanceAbstractFactory factory;

    public AccountService(TransactionService transactionService, FinanceAbstractFactory factory) {
        this.transactionService = transactionService;
        this.factory = factory;
    }

    /**
     * Creates and registers a new account.
     */
    public AccountInterface create(AccounType type, String name, Double balance)
            throws AccountOperationException, DataValidationException, UserLoginException {

        requireLoggedInUser();

        if (type == null || name == null || name.isBlank() || balance == null || balance < 0) {
            throw new AccountOperationException("Invalid input for account creation.");
        }

        try {
            AccountInterface account = factory.createAccount(type, name, balance);
            currentUser.addAccount(account);

            logger.info(() -> String.format("Account created for user='%s' (type=%s)",
                    currentUser.getName(), type));
            return account;

        } catch (Exception e) {
            throw new AccountOperationException("Account creation failed.", e);
        }
    }

    /**
     * Deletes a user's account and all related transactions.
     */
    public AccountInterface delete(AccountInterface account)
            throws AccountOperationException, UserLoginException {

        requireLoggedInUser();

        if (account == null) {
            throw new AccountOperationException("Cannot delete a null account.");
        }

        try {
            transactionService.removeTransactionsForAccount(account);
            currentUser.removeAccount(account);

            logger.info(() -> String.format("Account deleted (ID=%d)", account.getAccountId()));
            return account;

        } catch (Exception e) {
            throw new AccountOperationException("Failed to delete account.", e);
        }
    }

    /**
     * Modifies an existing account's name, type, or balance.
     */
    public AccountInterface modify(AccountInterface account, AccounType type, String newName, Double newBalance)
            throws DataValidationException, UserLoginException, AccountOperationException {

        requireLoggedInUser();

        if (account == null) {
            throw new AccountOperationException("Cannot modify a null account.");
        }

        try {
            if (newName != null && !newName.trim().isEmpty()) {
                account.setName(newName.trim());
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

            logger.info(() -> String.format("Account modified (ID=%d)", account.getAccountId()));
            return account;

        } catch (Exception e) {
            throw new AccountOperationException("Failed to modify account.", e);
        }
    }

    /**
     * Returns all accounts
     */
    public List<AccountInterface> getAllAccount() {
        return getCurrentUser().getAccountList();
    }
}
