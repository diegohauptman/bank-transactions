package com.hauptman.banktransactions;

import com.hauptman.banktransactions.dto.Transaction;
import com.hauptman.banktransactions.repository.TransactionRepository;
import com.hauptman.banktransactions.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class SearchTransactionsServiceTest {

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private TransactionService transactionService;
    private String accountIban;
    private List<Transaction> expectedTransactionsSortedAsc;
    private List<Transaction> expectedTransactionsSortedDesc;

    @BeforeEach
    public void setUp() {
        // given
        accountIban = "ES9820385778983000760236";
        Transaction t1 = new Transaction("ref1", accountIban, LocalDateTime.now(), BigDecimal.valueOf(50), BigDecimal.ZERO, "");
        Transaction t2 = new Transaction("ref2", accountIban, LocalDateTime.now().minusDays(2), BigDecimal.valueOf(75), BigDecimal.ZERO, "");
        Transaction t3 = new Transaction("ref3", accountIban, LocalDateTime.now().minusDays(1), BigDecimal.valueOf(100), BigDecimal.ZERO, "");

        transactionRepository.create(t1);
        transactionRepository.create(t2);
        transactionRepository.create(t3);

        expectedTransactionsSortedAsc = Arrays.asList(t1, t2, t3);
        expectedTransactionsSortedDesc = Arrays.asList(t3, t2, t1);
    }

    @Test
    public void testSearchTransactionsWithAccountIbanAndAscendingSort() {
        // when
        Sort.Direction sortDirection = Sort.Direction.ASC;
        List<Transaction> result = transactionService.searchTransactions(accountIban, sortDirection);
        // then
        assertEquals(expectedTransactionsSortedAsc, result);
    }

    @Test
    public void testSearchTransactionsWithoutAccountIbanAndDescendingSort() {
        // when
        Sort.Direction sortDirection = Sort.Direction.DESC;
        List<Transaction> result = transactionService.searchTransactions(accountIban, sortDirection);
        // then
        assertEquals(expectedTransactionsSortedDesc, result);
    }
}
