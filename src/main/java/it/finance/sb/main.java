package it.finance.sb;

import it.finance.sb.model.account.AccounType;
import it.finance.sb.model.account.AccountInterface;
import it.finance.sb.model.transaction.AbstractTransaction;
import it.finance.sb.model.transaction.TransactionType;
import it.finance.sb.model.user.Gender;
import it.finance.sb.model.user.User;
import it.finance.sb.service.AccountService;
import it.finance.sb.service.TransactionService;
import it.finance.sb.service.UserService;

import java.util.Date;

public class main {
    public static void main(String[] args) throws Exception {
        System.out.println("Ecco la tua app di finanze personale");

        //Create User
        UserService userService = new UserService();
        User user = userService.create("Stefano", 26, Gender.MALE);

        //Create Services
        TransactionService transactionService = new TransactionService();
        AccountService accountService = new AccountService(transactionService);

        //Create Account
        AccountInterface bper = accountService.create(AccounType.BANK, "Banca BPER", 2000.0);
        AccountInterface bper2 = accountService.create(AccounType.BANK, "Banca BPER", 2000.0);
        AccountInterface cash = accountService.create(AccounType.CASH, "Monete", 100.0);

        //Create Transaction
        AbstractTransaction rendimento = transactionService.create(TransactionType.INCOME, 50, "Rendimento", "Rendimento", new Date(), bper, null);
        AbstractTransaction rendimento2 = transactionService.create(TransactionType.INCOME, 50, "Rendimento2", "Rendimento", new Date(), bper, null);
        AbstractTransaction spesa = transactionService.create(TransactionType.EXPENSE, 200, "spesa", "spesa", new Date(), null, bper);
        AbstractTransaction spesa2 = transactionService.create(TransactionType.EXPENSE, 200, "spesa2", "spesa", new Date(), null, bper);
        AbstractTransaction video = transactionService.create(TransactionType.MOVEMENT, 200, "Ritiro videogiochi", "spesa", new Date(), cash, bper);
        AbstractTransaction video2 = transactionService.create(TransactionType.MOVEMENT, 200, "Ritiro videogiochi2", "spesa", new Date(), cash, bper);

        //Modify Account
        accountService.modify(bper, AccounType.CASH, "BPER", 2400.0);
        accountService.modify(bper, AccounType.BANK, "BPER", 2400.0);

        //DELETE Account
        accountService.delete(bper2);

        //Modify transaction

        //Delete transaction
        transactionService.delete(rendimento2);

        //Check reflexive account & transaction
        user.getAllAccountBalances();
        user.getAllTransactions();

    }
}
