package com.atm.service;

import com.atm.dao.Account;
import com.atm.dao.AccountRepository;
import com.atm.dao.Transaction;
import com.atm.dao.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
public class AccountService {
    // Tells the application context to inject an instance of AccountRepository here
    @Autowired
    AccountRepository accountRepository;
    // Tells the application context to inject an instance of TransactionRepository here
    @Autowired
    TransactionRepository transactionRepository;


    public List<Account> getAllAccounts() {
        List<Account> accounts = new ArrayList<>();
        accountRepository.findAll().forEach(account -> accounts.add(account));
        return accounts;
    }


    public Account getAccountByAccountNo(int accNo) {
        Account account = accountRepository.findByAccountNo(accNo);
        return account;
    }


    public void saveOrUpdate(Account account) {
        accountRepository.save(account);
    }

    public void delete(int acc_num) {
        accountRepository.deleteById(acc_num);
    }


    public int checkLogin(Account formAccount) {
        Account account = getAccountByAccountNo(formAccount.getAccountNo());
        //return 1 if account not found
        if (account == null) {
            return 1;
        } else if (account.getLocked() == 1) {
            return 3; // return 3 if account is locked
        } else if (account.getPin() != formAccount.getPin()) {
            if (account.getIncorrectAttempts() < 2) {
                account.setIncorrectAttempts(account.getIncorrectAttempts() + 1);
                saveOrUpdate(account);
                return 2; //return 2 if incorrect pin but account is not locked yet
            } else {
                account.setIncorrectAttempts(account.getIncorrectAttempts() + 1);
                account.setLocked(1);
                saveOrUpdate(account);
                return 3; //lock account on 3rd incorrect attempt and return 3
            }

        } else {
            return 0; // return 0 if login successful
        }

    }


    public boolean checkIfEnough(Account account, float amount) {
        float remaining = account.getBalance() - amount;
        if (remaining >= 0) {
            return true;
        }
        return false;
    }


    public boolean makeTransfer(int originAccNo, int targetAccNo, float amount) {
        Account origAccount = getAccountByAccountNo(originAccNo);
        Account targetAccount = getAccountByAccountNo(targetAccNo);
        if (checkIfEnough(origAccount, amount)) {
            origAccount.setBalance(origAccount.getBalance() - amount);
            targetAccount.setBalance(targetAccount.getBalance() + amount);
            //update the two accounts after the transaction
            saveOrUpdate(origAccount);
            saveOrUpdate(targetAccount);
            //save transaction to database with current timestamp
            Transaction transaction = new Transaction();
            transaction.setAccountNo(originAccNo);
            transaction.setTime(new Timestamp(new Date().getTime()));
            transaction.setType("transfer from");
            transaction.setAmount(amount);
            transactionRepository.save(transaction);
            Transaction transaction2 = new Transaction();
            transaction2.setAccountNo(targetAccNo);
            transaction2.setTime(new Timestamp(new Date().getTime()));
            transaction2.setType("transfer to");
            transaction2.setAmount(amount);
            transactionRepository.save(transaction2);

            return true;
        } else {
            return false;
        }
    }

    public boolean withdrawMoney(int accountNo, float amount) {
        Account account = getAccountByAccountNo(accountNo);
        //check if account has sufficient balance
        if (checkIfEnough(account, amount)) {
            account.setBalance(account.getBalance() - amount);
            saveOrUpdate(account);
            //save transaction to database
            Transaction transaction = new Transaction();
            transaction.setAccountNo(accountNo);
            transaction.setTime(new Timestamp(new Date().getTime()));
            transaction.setType("withdraw");
            transaction.setAmount(amount);
            transactionRepository.save(transaction);
            return true;
        }
        return false;
    }


    public boolean depositMoney(int accountNo, float amount) {
        Account account = getAccountByAccountNo(accountNo);
        account.setBalance(account.getBalance() + amount);
        saveOrUpdate(account);
        // save transaction to database
        Transaction transaction = new Transaction();
        transaction.setAccountNo(accountNo);
        transaction.setTime(new Timestamp(new Date().getTime()));
        transaction.setType("deposit");
        transaction.setAmount(amount);
        transactionRepository.save(transaction);
        return true;
    }

    public float getBalance(int accountNum) {
        Account account = getAccountByAccountNo(accountNum);
        return account.getBalance();
    }

}
