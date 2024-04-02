package com.customs.network.fdapn.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FinalCount<T> {
    private long totalValidationErrorCount;
    private long totalAcceptedCount;
    private long totalRejectedCount;
    private long totalPendingCount;
    private long totalCbpDownCount;
    private long allTransactions;
    List<T> totalTransactionCountDtos;
    List<DailyAuditDTO> dailyAuditData;
    Map<String,T> trendsData;
}
