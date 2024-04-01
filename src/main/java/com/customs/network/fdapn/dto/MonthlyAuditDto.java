package com.customs.network.fdapn.dto;

import lombok.Data;

@Data
public class MonthlyAuditDto {
    private Long id;
    private String userId;
    private String month;
    private Long acceptedCount;
    private Long rejectedCount;
    private Long pendingCount;
    private Long validationErrorCount;
    private Long cbpDownCount;
    private Long totalTransactions;
}
