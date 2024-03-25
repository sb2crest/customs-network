package com.customs.network.fdapn.dto;

import lombok.Data;

import java.util.List;

@Data
public class TotalTransactionCountDto {
    private long validationErrorCount;
    private long acceptedCount;
    private long rejectedCount;
    private long pendingCount;
    private long cbpDownCount;
    private long totalTransactions;
    private String date;
    List<DailyAuditDTO> dailyAuditDTOS;
}
