package com.hauptman.banktransactions.service;

import com.hauptman.banktransactions.dto.Account;
import com.hauptman.banktransactions.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    @Autowired
    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account getAccountByIban(String iban) {
        return accountRepository.findByIban(iban);
    }

    public void updateAccountBalance(String iban, BigDecimal amount) {
        Account account = accountRepository.findByIban(iban);
        BigDecimal balance = account.getBalance().add(amount);
        account.setBalance(balance);
        accountRepository.save(account);
    }

    public void createAccount(Account account) {
        if (account.getIban() == null || account.getIban().isEmpty()) {
            throw new IllegalArgumentException("Account IBAN is required");
        }
        if (account.getBalance() == null || account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Account balance must be a positive number");
        }

        accountRepository.save(account);
    }


}
