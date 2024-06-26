package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.*;


import java.text.ParseException;
import java.util.List;


public interface AuditService {
    FinalCount<TotalTransactionCountDto<?>> getUserTransactionsForPeriod(String userId, String period);
    FinalCount<TotalTransactionCountDto<?>> getAllTransactionsCounts(String userId, String period);
    List<PortInfoDto> getPortTransactionInfoByUser(String userId, String portName, String portCode);
    void auditAndUpdateMonthlyAuditTable() throws ParseException;

    void auditAndUpdateYearlyAuditTable();
}
