package com.customs.network.fdapn.dto;

import lombok.Data;

@Data
public class DailyAuditDTO {
    private long id;
    private String userId;
    private String date;
    private long acceptedCount;
    private long rejectedCount;
    private long pendingCount;
    private long cbpDownCount;
    private long totalTransactions;
}
