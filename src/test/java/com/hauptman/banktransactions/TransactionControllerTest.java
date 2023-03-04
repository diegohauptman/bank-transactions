package com.hauptman.banktransactions;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hauptman.banktransactions.dto.Account;
import com.hauptman.banktransactions.dto.Transaction;
import com.hauptman.banktransactions.dto.TransactionRequest;
import com.hauptman.banktransactions.repository.AccountRepository;
import com.hauptman.banktransactions.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TransactionControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private AccountRepository accountRepository;

    @BeforeEach
    public void setup() {
        Account account1 = new Account("ES9820385778983000760236", new BigDecimal("1000.00"));
        accountRepository.save(account1);
    }

    @Test
    public void testCreateTransaction() throws Exception {
        TransactionRequest request = new TransactionRequest();
        request.setAccountIban("ES9820385778983000760236");
        request.setAmount(BigDecimal.valueOf(200));
        request.setFee(BigDecimal.valueOf(3.18));
        request.setDescription("Restaurant payment");
        request.setReference("12345A");
        request.setDate(LocalDateTime.now());
        mockMvc.perform(MockMvcRequestBuilders.post("/transactions/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    public void testSearchTransactionsAscendingSort() throws Exception {
        // given
        String accountIban = "ES9820385778983000760236";
        List<Transaction> expectedTransactions = getTransactions(accountIban, "ASC");

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/transactions/search")
                        .param("account_iban", accountIban)
                        .param("sort_direction", "ASC")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // then
        List<Transaction> transactions = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertEquals(expectedTransactions, transactions);
    }

    @Test
    public void testSearchTransactionsDescendingSort() throws Exception {
        // given
        String accountIban = "ES9820385778983000760236";
        List<Transaction> expectedTransactions = getTransactions(accountIban, "DESC");

        // when
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/transactions/search")
                        .param("account_iban", accountIban)
                        .param("sort_direction", "DESC")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        // then
        List<Transaction> transactions = objectMapper.readValue(result.getResponse().getContentAsString(),
                new TypeReference<>() {
                });
        assertEquals(expectedTransactions, transactions);

    }

    private List<Transaction> getTransactions(String accountIban, String sortDirection) {
        TransactionRequest request1 = new TransactionRequest();
        request1.setReference("ref1");
        request1.setAccountIban(accountIban);
        request1.setAmount(BigDecimal.valueOf(50));
        request1.setFee(BigDecimal.ZERO);
        request1.setDescription("");
        request1.setDate(LocalDateTime.now());

        TransactionRequest request2 = new TransactionRequest();
        request2.setReference("ref2");
        request2.setAccountIban(accountIban);
        request2.setAmount(BigDecimal.valueOf(75));
        request2.setFee(BigDecimal.ZERO);
        request2.setDescription("");
        request2.setDate(LocalDateTime.now().minusDays(2));

        TransactionRequest request3 = new TransactionRequest();
        request3.setReference("ref3");
        request3.setAccountIban(accountIban);
        request3.setAmount(BigDecimal.valueOf(100));
        request3.setFee(BigDecimal.ZERO);
        request3.setDescription("");
        request3.setDate(LocalDateTime.now().minusDays(1));

        transactionService.createTransaction(request1);
        transactionService.createTransaction(request2);
        transactionService.createTransaction(request3);

        // get transactions by account iban
        List<Transaction> transactions = transactionService.searchTransactions(accountIban, null);

        // sort transactions based on sort direction
        if (sortDirection.equalsIgnoreCase("ASC")) {
            transactions.sort(Comparator.comparing(Transaction::getAmount));
        } else {
            transactions.sort(Comparator.comparing(Transaction::getAmount).reversed());
        }

        return transactions;
    }

}

