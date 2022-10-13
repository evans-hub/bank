package com.atm.controller;

import com.atm.dao.Account;
import com.atm.dao.Transaction;
import com.atm.service.AccountService;
import com.atm.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;

@Controller
public class ATMController {

    // Tells the application context to inject an instance of AccountService here
    @Autowired
    AccountService accountService;
    @Autowired
    TransactionService transactionService;


    private List<String> tasks = Arrays.asList("View Balance", "Withdraw", "Deposit", "Transfer", "Exit");

    private String custName; 
    private Account theAccount;


    @GetMapping("/")
    public String main(Model model) {

        model.addAttribute("tasks", tasks);
        model.addAttribute("account", new Account());

        return "welcome"; //view
    }


    @GetMapping("/hello")
    public String mainWithParam(
            @RequestParam(name = "name", required = false, defaultValue = "") String name, Model model) {

        custName = name;

        model.addAttribute("message", custName);
        return "welcome"; //view
    }

    @PostMapping("/login")
    public String login(@ModelAttribute Account formAccount, Model model, HttpSession session) {
        //callling the appropriate service function which performs login
        int result = accountService.checkLogin(formAccount);
        //depending on the return value, add messages to the model which will be viewed
        if (result == 1) {
            model.addAttribute("error", "Account number does not exist");
            System.out.println("Login error");
            return "welcome";
        } else if (result == 2) {
            model.addAttribute("error", "Incorrect pin");
            System.out.println("Login error");
            return "welcome";
        } else if (result == 3) {
            model.addAttribute("error", "Account locked");
            System.out.println("Login error");
            return "welcome";
        }
        else {
            System.out.println("Login successful");
            //set session if login is successful and return menu page
            session.setAttribute("accountNum", formAccount.getAccountNo());
            return "menu";
        }
    }


    @GetMapping("/balance")
    public String mainBalance(Model model,  HttpSession session) {
        
        float bal = accountService.getAccountByAccountNo((int)session.getAttribute("accountNum")).getBalance();
        model.addAttribute("balance", bal);    
        return "balance"; //view
    }

    @GetMapping("/withdraw")
    public String menu(Model model, HttpSession session) {
        model.addAttribute("account", new Account());
        int accnum = (int)session.getAttribute("accountNum");
        model.addAttribute("accnum", accnum);
        return "withdraw"; //view
    }



    @GetMapping("/accounts")
    private String getAllAccounts(Model model) {
        List<Account> accounts = accountService.getAllAccounts();
        model.addAttribute("accounts", accounts);
        return "welcome";
    }


    @GetMapping("/menu")
    public String goToMenu() {
        return "menu";
    }

    @GetMapping("/transfer")
    public String goToTransfer(Model model) {
        model.addAttribute("account", new Account());
        return "transfer";
    }

    @GetMapping("/logout")
    public String logout(Model model,HttpSession session) {
        session.invalidate();
        model.asMap().clear();
        //session.removeAttribute("account");
        model.addAttribute("account", new Account());
        return "welcome";
      }


    @PostMapping("/transfer")
    public String makeTransfer(@ModelAttribute Account targetAccount, HttpSession session, Model model) {
        //calls the appropriate service function to transfer money
        boolean result = accountService.makeTransfer((Integer)session.getAttribute("accountNum"),
                targetAccount.getAccountNo(), targetAccount.getBalance());
        Transaction transaction =  transactionService.getTransaction();
        float balance = accountService.getBalance((Integer)session.getAttribute("accountNum"));
        if (result) {
            model.addAttribute("message", "Transfer was successful");
            model.addAttribute("transaction", transaction);
            model.addAttribute("accountNo", transaction.getAccountNo());
            model.addAttribute("transferred", transaction.getAmount());
            model.addAttribute("balance", balance);
        } else {
            model.addAttribute("message", "Low balance. Could not transfer. ");
        }
        return "transfer";
    }


    @PostMapping("/withdraw")
    public String withdrawMoney(@ModelAttribute Account account, HttpSession session, Model model) {
        int accountNum = (Integer)session.getAttribute("accountNum");
        boolean result = accountService.withdrawMoney(accountNum,
                account.getBalance());
        float balance = accountService.getBalance(accountNum);
        if (result) {
            model.addAttribute("message", "You withdrew $" + account.getBalance());
            model.addAttribute("balance", balance);
            model.addAttribute("accountNum", accountNum);
        } else {
            model.addAttribute("message", "Low balance. Could not withdraw. ");
        }
        return "withdraw";
    }


    @GetMapping("/deposit")
    public String deposit(Model model) {
        model.addAttribute("account", new Account());
        return "deposit"; //view
    }

 
    @PostMapping("/deposit")
    public String depositMoney(@ModelAttribute Account account, HttpSession session, Model model) {
        int accountNum = (Integer)session.getAttribute("accountNum");
        boolean result = accountService.depositMoney(accountNum,
                account.getBalance());
        float balance = accountService.getBalance(accountNum);
        if (result) {
            model.addAttribute("message", "You deposited $" + account.getBalance());
            model.addAttribute("balance", balance);
            model.addAttribute("accountNum", accountNum);
        } else {
            model.addAttribute("message", "Error while depositing.");
        }
        return "deposit";
    }

}