package com.hauptman.banktransactions.service;

import com.hauptman.banktransactions.dto.*;
import com.hauptman.banktransactions.exception.TransactionCreationException;
import com.hauptman.banktransactions.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import util.ChannelType;
import util.TransactionStatusEnum;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, AccountService accountService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
    }

    public Transaction createTransaction(TransactionRequest request) {
        if (request.getAccountIban() == null || request.getAccountIban().isEmpty()) {
            throw new IllegalArgumentException("Account IBAN is required");
        }
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transaction amount must be a positive number");
        }
        String reference = request.getReference() != null ? request.getReference() : UUID.randomUUID().toString();
        String accountIban = request.getAccountIban();
        BigDecimal amount = request.getAmount();
        BigDecimal fee = request.getFee() != null ? request.getFee() : BigDecimal.ZERO;
        String description = request.getDescription() != null ? request.getDescription() : "";
        LocalDateTime date = request.getDate() != null ? request.getDate() : LocalDateTime.now();

        Account account = accountService.getAccountByIban(accountIban);

        if (account.getBalance().subtract(amount).subtract(fee).compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Transaction not allowed: insufficient funds");
        }

        Transaction transaction = new Transaction(reference, accountIban, date, amount, fee, description);

        try {
            transactionRepository.create(transaction);
            accountService.updateAccountBalance(accountIban, amount.negate().subtract(fee));
        } catch (Exception ex) {
            throw new TransactionCreationException("Failed to create transaction", ex);
        }

        return transaction;
    }

    public List<Transaction> searchTransactions(String accountIban, Sort.Direction sortDirection) {
        boolean sortAscending = sortDirection == Sort.Direction.ASC;
        return transactionRepository.findByAccountIban(accountIban, sortAscending);
    }

    public TransactionStatusResponse getTransactionStatus(TransactionStatusRequest request) {
        String reference = request.getReference();
        ChannelType channel = request.getChannel();

        Transaction transaction = transactionRepository.findByReference(reference);

        if (transaction == null) {
            return new TransactionStatusResponse(reference, TransactionStatusEnum.INVALID, null, null);
        }

        LocalDateTime transactionDate = transaction.getDate();
        LocalDateTime currentDate = LocalDateTime.now();

        BigDecimal amount = transaction.getAmount();
        BigDecimal fee = transaction.getFee();

        if (transactionDate.isBefore(currentDate.toLocalDate().atStartOfDay())) {
            if (channel == ChannelType.CLIENT || channel == ChannelType.ATM) {
                return new TransactionStatusResponse(reference, TransactionStatusEnum.SETTLED, amount.subtract(fee), null);
            } else if (channel == ChannelType.INTERNAL) {
                return new TransactionStatusResponse(reference, TransactionStatusEnum.SETTLED, amount, fee);
            }
        } else if (transactionDate.isBefore(currentDate) || transactionDate.equals(currentDate)) {
            if (channel == ChannelType.CLIENT || channel == ChannelType.ATM) {
                return new TransactionStatusResponse(reference, TransactionStatusEnum.PENDING, amount.subtract(fee), null);
            } else if (channel == ChannelType.INTERNAL) {
                return new TransactionStatusResponse(reference, TransactionStatusEnum.PENDING, amount, fee);
            }
        } else if (transactionDate.isAfter(currentDate.toLocalDate().atStartOfDay())) {
            if (channel == ChannelType.CLIENT) {
                return new TransactionStatusResponse(reference, TransactionStatusEnum.FUTURE, amount.subtract(fee), null);
            } else if (channel == ChannelType.ATM) {
                return new TransactionStatusResponse(reference, TransactionStatusEnum.PENDING, amount.subtract(fee), null);
            } else if (channel == ChannelType.INTERNAL) {
                return new TransactionStatusResponse(reference, TransactionStatusEnum.FUTURE, amount, fee);
            }
        }

        // Should never reach this line
        throw new IllegalStateException("Invalid transaction status");
    }

}

