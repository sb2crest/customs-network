package com.customs.network.fdapn.repository;


import com.customs.network.fdapn.dto.FilterCriteriaDTO;
import com.customs.network.fdapn.dto.PageDTO;
import com.customs.network.fdapn.exception.BatchInsertionException;
import com.customs.network.fdapn.model.CustomsFdapnSubmit;

import java.util.List;

public interface TransactionRepository {
    CustomsFdapnSubmit saveTransaction(CustomsFdapnSubmit request);

    List<CustomsFdapnSubmit> saveTransaction(List<CustomsFdapnSubmit> requestList) throws BatchInsertionException;

    CustomsFdapnSubmit fetchTransaction(String refId);

    void changeTransactionStatus(String refId, String newStatus);

    PageDTO<CustomsFdapnSubmit> fetchTransactionPages(FilterCriteriaDTO request);

    PageDTO<CustomsFdapnSubmit> fetchByFilter(FilterCriteriaDTO filterRequest);

    PageDTO<CustomsFdapnSubmit> scanSchemaByColValue(String fieldName,String value,String startDate,String endDate,String userId,int page, int size);
}
