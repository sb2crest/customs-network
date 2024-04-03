package com.customs.network.fdapn.dto;

import lombok.Data;

@Data
public class YearlyAuditDto {
    private Long id;
    private String userId;
    private String year;
    private Long acceptedCount;
    private Long rejectedCount;
    private Long pendingCount;
    private Long validationErrorCount;
    private Long cbpDownCount;
    private Long totalTransactions;
}
