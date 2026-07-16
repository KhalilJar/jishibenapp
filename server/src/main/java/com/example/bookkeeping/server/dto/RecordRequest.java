package com.example.bookkeeping.server.dto;

import lombok.Data;

@Data
public class RecordRequest {
    private String type;        // INCOME 或 EXPENSE
    private Double amount;
    private String tag;
    private String note;
    private Long timestamp;
    private Long accountId;
}
