package com.hauptman.banktransactions.repository;

import com.hauptman.banktransactions.dto.Transaction;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository {
    void create(Transaction transaction);
    Transaction findByReference(String reference);
    List<Transaction> findByAccountIban(String accountIban, boolean sortAscending);
}

