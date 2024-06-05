package com.customs.network.fdapn.repository;

import com.customs.network.fdapn.dto.FilterCriteriaDTO;
import com.customs.network.fdapn.dto.PageDTO;
import com.customs.network.fdapn.exception.BatchInsertionException;
import com.customs.network.fdapn.model.TransactionInfo;

import java.util.List;

public interface TransactionManagerRepo {
    TransactionInfo saveTransaction(TransactionInfo request);

    List<TransactionInfo> saveTransaction(List<TransactionInfo> requestList) throws BatchInsertionException;

    TransactionInfo fetchTransaction(String refId);

    void changeTransactionStatus(String refId, String newStatus);

    PageDTO<TransactionInfo> fetchTransactionPages(FilterCriteriaDTO filterRequest);

    PageDTO<TransactionInfo> fetchByFilter(FilterCriteriaDTO filterRequest);

    PageDTO<TransactionInfo> scanSchemaByColValue(String fieldName, String value, String startDate, String endDate, String userId, int page, int size);
}
