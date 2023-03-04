package com.hauptman.banktransactions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {
    private String reference;
    private String accountIban;
    private LocalDateTime date;
    private BigDecimal amount;
    private BigDecimal fee;
    private String description;
}

