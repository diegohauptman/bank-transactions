package com.hauptman.banktransactions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import util.TransactionStatusEnum;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionStatusResponse {
    private String reference;
    private TransactionStatusEnum status;
    private BigDecimal amount;
    private BigDecimal fee;
}
