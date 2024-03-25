package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.DailyAuditDTO;
import com.customs.network.fdapn.dto.FinalCount;
import com.customs.network.fdapn.dto.TotalTransactionCountDto;

import java.util.List;

public interface AuditService {
    List<DailyAuditDTO> getUserTransactionsForWeek(String userId, String period);
    FinalCount getAllTransactionsCounts(String userId, String period);

}
