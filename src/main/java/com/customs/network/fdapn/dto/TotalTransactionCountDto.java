package com.customs.network.fdapn.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TotalTransactionCountDto <T>{
    private long validationErrorCount;
    private long acceptedCount;
    private long rejectedCount;
    private long pendingCount;
    private long cbpDownCount;
    private long totalTransactions;
    private String date;
    List<T> auditData;
}
