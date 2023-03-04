package com.hauptman.banktransactions.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionRequest {
    private String reference;
    private String accountIban;
    private LocalDateTime date;
    private BigDecimal amount;
    private BigDecimal fee;
    private String description;
}

