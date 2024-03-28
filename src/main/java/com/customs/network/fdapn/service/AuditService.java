package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.FinalCount;
import com.customs.network.fdapn.dto.FinalCountForUser;
import com.customs.network.fdapn.dto.PortInfoDto;

import java.util.List;


public interface AuditService {
    FinalCountForUser getUserTransactionsForPeriod(String userId, String period);
    FinalCount getAllTransactionsCounts(String userId, String period);
    List<PortInfoDto> getByUser(String userId,String portCode);
}
