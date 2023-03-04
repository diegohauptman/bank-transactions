package com.hauptman.banktransactions.repository;

import com.hauptman.banktransactions.dto.Account;

public interface AccountRepository {
    void save(Account account);
    Account findByIban(String iban);
}
