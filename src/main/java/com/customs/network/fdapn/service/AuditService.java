package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.FinalCount;
import com.customs.network.fdapn.dto.FinalCountForUser;


public interface AuditService {
    FinalCountForUser getUserTransactionsForPeriod(String userId, String period);
    FinalCount getAllTransactionsCounts(String userId, String period);

}
