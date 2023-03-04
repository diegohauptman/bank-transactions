package com.hauptman.banktransactions;

import com.hauptman.banktransactions.dto.*;
import com.hauptman.banktransactions.service.AccountService;
import com.hauptman.banktransactions.service.TransactionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import util.ChannelType;
import util.TransactionStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionStatusControllerTest {

    public static final String TRANSACTION_STATUS_ENDPOINT = "/transactions/status";
    public static final String TRANSACTION_STATUS_PARAMS = "?reference={reference}&channel={channel}";
    public static final String TRANSACTION_STATUS_URL = TRANSACTION_STATUS_ENDPOINT + TRANSACTION_STATUS_PARAMS;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private TestRestTemplate testRestTemplate;

    // 1
    @Test
    public void testTransactionNotFound() {
        // given
        TransactionStatusRequest request = new TransactionStatusRequest();
        request.setReference("XXXXXX");
        request.setChannel(ChannelType.CLIENT);

        // when
        ResponseEntity<TransactionStatusResponse> response = testRestTemplate.getForEntity(
                TRANSACTION_STATUS_URL,
                TransactionStatusResponse.class,
                request.getReference(),
                request.getChannel()
        );

        // then
        assertEquals(request.getReference(), response.getBody().getReference());
        assertEquals(TransactionStatusEnum.INVALID, response.getBody().getStatus());
    }

    // 2
    @Test
    public void testTransactionSettledBeforeToday() {
        // given
        Account account = new Account("ES123456789", new BigDecimal("200.00"));
        accountService.createAccount(account);

        TransactionRequest request = new TransactionRequest();
        request.setAccountIban("ES123456789");
        request.setAmount(new BigDecimal("100.00"));
        request.setFee(new BigDecimal("5.00"));
        request.setDate(LocalDateTime.of(2022, 2, 15, 10, 30));
        Transaction transaction = transactionService.createTransaction(request);

        TransactionStatusRequest statusRequestClient = new TransactionStatusRequest();
        statusRequestClient.setReference(transaction.getReference());
        statusRequestClient.setChannel(ChannelType.CLIENT);

        TransactionStatusRequest statusRequestATM = new TransactionStatusRequest();
        statusRequestATM.setReference(transaction.getReference());
        statusRequestATM.setChannel(ChannelType.ATM);

        // when
        ResponseEntity<TransactionStatusResponse> responseClient = testRestTemplate.getForEntity(
                TRANSACTION_STATUS_URL,
                TransactionStatusResponse.class,
                statusRequestClient.getReference(),
                statusRequestClient.getChannel()
        );

        ResponseEntity<TransactionStatusResponse> responseATM = testRestTemplate.getForEntity(
                TRANSACTION_STATUS_URL,
                TransactionStatusResponse.class,
                statusRequestATM.getReference(),
                statusRequestATM.getChannel()
        );

        // then
        assertEquals(statusRequestClient.getReference(), responseClient.getBody().getReference());
        assertEquals(TransactionStatusEnum.SETTLED, responseClient.getBody().getStatus());
        assertEquals(new BigDecimal("95.00"), responseClient.getBody().getAmount());
        assertNull(responseClient.getBody().getFee());

        assertEquals(statusRequestATM.getReference(), responseATM.getBody().getReference());
        assertEquals(TransactionStatusEnum.SETTLED, responseATM.getBody().getStatus());
        assertEquals(new BigDecimal("95.00"), responseATM.getBody().getAmount());
        assertNull(responseATM.getBody().getFee());
    }

    // 3
    @Test
    public void testTransactionSettledBeforeTodayFromInternalChannel() {
        // given
        Account account = new Account("ES123456789", new BigDecimal("200.00"));
        accountService.createAccount(account);

        TransactionRequest request = new TransactionRequest();
        request.setAccountIban("ES123456789");
        request.setAmount(new BigDecimal("100.00"));
        request.setFee(new BigDecimal("5.00"));
        request.setDate(LocalDateTime.of(2022, 2, 15, 10, 30));
        Transaction transaction = transactionService.createTransaction(request);

        TransactionStatusRequest statusRequest = new TransactionStatusRequest();
        statusRequest.setReference(transaction.getReference());
        statusRequest.setChannel(ChannelType.INTERNAL);

        // when
        ResponseEntity<TransactionStatusResponse> response = testRestTemplate.getForEntity(
                TRANSACTION_STATUS_URL,
                TransactionStatusResponse.class,
                statusRequest.getReference(),
                statusRequest.getChannel()
        );

        // then
        assertEquals(statusRequest.getReference(), response.getBody().getReference());
        assertEquals(TransactionStatusEnum.SETTLED, response.getBody().getStatus());
        assertEquals(new BigDecimal("100.00"), response.getBody().getAmount());
        assertEquals(new BigDecimal("5.00"), response.getBody().getFee());
    }

    // 4
    @Test
    public void testTransactionPendingTodayFromClientOrAtmChannel() {
        // given
        Account account = new Account("ES123456789", new BigDecimal("200.00"));
        accountService.createAccount(account);

        TransactionRequest request = new TransactionRequest();
        request.setAccountIban("ES123456789");
        request.setAmount(new BigDecimal("100.00"));
        request.setFee(new BigDecimal("5.00"));
        request.setDate(LocalDateTime.now());
        Transaction transaction = transactionService.createTransaction(request);

        TransactionStatusRequest statusRequestClient = new TransactionStatusRequest();
        statusRequestClient.setReference(transaction.getReference());
        statusRequestClient.setChannel(ChannelType.CLIENT);

        TransactionStatusRequest statusRequestATM = new TransactionStatusRequest();
        statusRequestATM.setReference(transaction.getReference());
        statusRequestATM.setChannel(ChannelType.ATM);

        // when
        ResponseEntity<TransactionStatusResponse> responseClient = testRestTemplate.getForEntity(
                TRANSACTION_STATUS_URL,
                TransactionStatusResponse.class,
                statusRequestClient.getReference(),
                statusRequestClient.getChannel()
        );

        ResponseEntity<TransactionStatusResponse> responseATM = testRestTemplate.getForEntity(
                TRANSACTION_STATUS_URL,
                TransactionStatusResponse.class,
                statusRequestATM.getReference(),
                statusRequestATM.getChannel()
        );

        // then
        assertEquals(statusRequestClient.getReference(), responseClient.getBody().getReference());
        assertEquals(TransactionStatusEnum.PENDING, responseClient.getBody().getStatus());
        assertEquals(new BigDecimal("95.00"), responseClient.getBody().getAmount());
        assertNull(responseClient.getBody().getFee());

        assertEquals(statusRequestATM.getReference(), responseATM.getBody().getReference());
        assertEquals(TransactionStatusEnum.PENDING, responseATM.getBody().getStatus());
        assertEquals(new BigDecimal("95.00"), responseATM.getBody().getAmount());
        assertNull(responseATM.getBody().getFee());
    }

    // 5
    @Test
    public void testTransactionPendingTodayFromInternalChannel() {
        // given
        Account account = new Account("ES123456789", new BigDecimal("200.00"));
        accountService.createAccount(account);

        TransactionRequest request = new TransactionRequest();
        request.setAccountIban("ES123456789");
        request.setAmount(new BigDecimal("100.00"));
        request.setFee(new BigDecimal("5.00"));
        request.setDate(LocalDateTime.now());
        Transaction transaction = transactionService.createTransaction(request);

        TransactionStatusRequest statusRequest = new TransactionStatusRequest();
        statusRequest.setReference(transaction.getReference());
        statusRequest.setChannel(ChannelType.INTERNAL);

        // when
        ResponseEntity<TransactionStatusResponse> response = testRestTemplate.getForEntity(
                TRANSACTION_STATUS_URL,
                TransactionStatusResponse.class,
                statusRequest.getReference(),
                statusRequest.getChannel()
        );

        // then
        assertEquals(statusRequest.getReference(), response.getBody().getReference());
        assertEquals(TransactionStatusEnum.PENDING, response.getBody().getStatus());
        assertEquals(new BigDecimal("100.00"), response.getBody().getAmount());
        assertEquals(new BigDecimal("5.00"), response.getBody().getFee());
    }

    // 6
    @Test
    public void testTransactionFutureFromClientChannel() {
        // given
        Account account = new Account("ES123456789", new BigDecimal("200.00"));
        accountService.createAccount(account);

        TransactionRequest request = new TransactionRequest();
        request.setAccountIban("ES123456789");
        request.setAmount(new BigDecimal("100.00"));
        request.setFee(new BigDecimal("5.00"));
        request.setDate(LocalDateTime.now().plusDays(1));
        Transaction transaction = transactionService.createTransaction(request);

        TransactionStatusRequest statusRequest = new TransactionStatusRequest();
        statusRequest.setReference(transaction.getReference());
        statusRequest.setChannel(ChannelType.CLIENT);

        // when
        ResponseEntity<TransactionStatusResponse> response = testRestTemplate.getForEntity(
                TRANSACTION_STATUS_URL,
                TransactionStatusResponse.class,
                statusRequest.getReference(),
                statusRequest.getChannel()
        );

        // then
        assertEquals(statusRequest.getReference(), response.getBody().getReference());
        assertEquals(TransactionStatusEnum.FUTURE, response.getBody().getStatus());
        assertEquals(new BigDecimal("95.00"), response.getBody().getAmount());
        assertNull(response.getBody().getFee());
    }

    // 7
    @Test
    public void testTransactionFutureFromATMChannel() {
        // given
        Account account = new Account("ES123456789", new BigDecimal("200.00"));
        accountService.createAccount(account);

        TransactionRequest request = new TransactionRequest();
        request.setAccountIban("ES123456789");
        request.setAmount(new BigDecimal("100.00"));
        request.setFee(new BigDecimal("5.00"));
        request.setDate(LocalDateTime.now().plusDays(1));
        Transaction transaction = transactionService.createTransaction(request);

        TransactionStatusRequest statusRequest = new TransactionStatusRequest();
        statusRequest.setReference(transaction.getReference());
        statusRequest.setChannel(ChannelType.ATM);

        // when
        ResponseEntity<TransactionStatusResponse> response = testRestTemplate.getForEntity(
                TRANSACTION_STATUS_URL,
                TransactionStatusResponse.class,
                statusRequest.getReference(),
                statusRequest.getChannel()
        );

        // then
        assertEquals(statusRequest.getReference(), response.getBody().getReference());
        assertEquals(TransactionStatusEnum.PENDING, response.getBody().getStatus());
        assertEquals(new BigDecimal("95.00"), response.getBody().getAmount());
        assertNull(response.getBody().getFee());
    }

    // 8
    @Test
    public void testTransactionFutureFromInternalChannel() {
        // given
        Account account = new Account("ES123456789", new BigDecimal("200.00"));
        accountService.createAccount(account);

        TransactionRequest request = new TransactionRequest();
        request.setAccountIban("ES123456789");
        request.setAmount(new BigDecimal("100.00"));
        request.setFee(new BigDecimal("5.00"));
        request.setDate(LocalDateTime.now().plusDays(1));
        Transaction transaction = transactionService.createTransaction(request);

        TransactionStatusRequest statusRequest = new TransactionStatusRequest();
        statusRequest.setReference(transaction.getReference());
        statusRequest.setChannel(ChannelType.INTERNAL);

        // when
        ResponseEntity<TransactionStatusResponse> response = testRestTemplate.getForEntity(
                TRANSACTION_STATUS_URL,
                TransactionStatusResponse.class,
                statusRequest.getReference(),
                statusRequest.getChannel()
        );

        // then
        assertEquals(statusRequest.getReference(), response.getBody().getReference());
        assertEquals(TransactionStatusEnum.FUTURE, response.getBody().getStatus());
        assertEquals(new BigDecimal("100.00"), response.getBody().getAmount());
        assertEquals(new BigDecimal("5.00"), response.getBody().getFee());
    }
}

