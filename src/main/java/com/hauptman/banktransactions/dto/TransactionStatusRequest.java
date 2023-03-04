package com.hauptman.banktransactions.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import util.ChannelType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionStatusRequest {
    private String reference;
    private ChannelType channel;
}
