package com.customs.network.fdapn.dto;

import lombok.Data;

import java.util.List;

@Data
public class FinalCountForUser {
    private long totalAcceptedCount;
    private long totalRejectedCount;
    private long totalPendingCount;
    private long totalCbpDownCount;
    private long allTransactions;
    private List<DailyAuditDTO> dailyAuditDTOS;
}
