package com.hauptman.banktransactions.repository;

import com.hauptman.banktransactions.dto.Account;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InMemoryAccountRepository implements AccountRepository {

    private final Map<String, Account> accounts = new ConcurrentHashMap<>();

    @Override
    public void save(Account account) {
        accounts.put(account.getIban(), account);
    }

    @Override
    public Account findByIban(String iban) {
        Account account = accounts.get(iban);
        if (account == null) {
            throw new IllegalArgumentException("Account not found");
        }
        return account;
    }

}

