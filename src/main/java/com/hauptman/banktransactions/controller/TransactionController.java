package com.hauptman.banktransactions.controller;

import com.hauptman.banktransactions.dto.*;
import com.hauptman.banktransactions.exception.TransactionCreationException;
import com.hauptman.banktransactions.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createTransaction(@RequestBody TransactionRequest request) {
        try {
            Transaction transaction = transactionService.createTransaction(request);
            return new ResponseEntity<>(transaction, HttpStatus.CREATED);
        } catch (IllegalArgumentException ex) {
            return new ResponseEntity<>("Invalid transaction request: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (TransactionCreationException ex) {
            return new ResponseEntity<>("Failed to create transaction: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    public List<Transaction> searchTransactions(
            @RequestParam(name = "account_iban", required = false) String accountIban,
            @RequestParam(name = "sort_direction", defaultValue = "ASC") Sort.Direction sortDirection) {
        return transactionService.searchTransactions(accountIban, sortDirection);
    }

    @GetMapping("/status")
    public ResponseEntity<TransactionStatusResponse> getTransactionStatus(
            @ModelAttribute TransactionStatusRequest request) {
        TransactionStatusResponse response = transactionService.getTransactionStatus(request);
        return ResponseEntity.ok(response);
    }

}
