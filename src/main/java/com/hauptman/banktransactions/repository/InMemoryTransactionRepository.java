package com.hauptman.banktransactions.repository;

import com.hauptman.banktransactions.dto.Transaction;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class InMemoryTransactionRepository implements TransactionRepository {

    private final Map<String, Transaction> transactions = new ConcurrentHashMap<>();

    @Override
    public void create(Transaction transaction) {
        transactions.put(transaction.getReference(), transaction);
    }

    @Override
    public Transaction findByReference(String reference) {
        for (Transaction transaction : transactions.values()) {
            if (transaction.getReference().equals(reference)) {
                return transaction;
            }
        }
        return null;
    }


    @Override
    public List<Transaction> findByAccountIban(String accountIban, boolean sortAscending) {
        List<Transaction> filteredTransactions = transactions.values()
                .stream()
                .filter(t -> t.getAccountIban().equals(accountIban))
                .collect(Collectors.toList());
        if (sortAscending) {
            filteredTransactions.sort(Comparator.comparing(Transaction::getAmount));
        } else {
            filteredTransactions.sort(Comparator.comparing(Transaction::getAmount).reversed());
        }
        return filteredTransactions;
    }

}
