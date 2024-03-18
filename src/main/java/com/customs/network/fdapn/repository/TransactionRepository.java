package com.customs.network.fdapn.repository;


import com.customs.network.fdapn.dto.FilterCriteriaDTO;
import com.customs.network.fdapn.dto.PageDTO;
import com.customs.network.fdapn.model.CustomsFdapnSubmit;

import java.util.List;

public interface TransactionRepository {
    CustomsFdapnSubmit saveTransaction(CustomsFdapnSubmit request);

    CustomsFdapnSubmit fetchTransaction(String refId);

    PageDTO<CustomsFdapnSubmit> fetchTransactionPages(FilterCriteriaDTO request);

    PageDTO<CustomsFdapnSubmit> fetchByFilter(FilterCriteriaDTO filterRequest);

    List<CustomsFdapnSubmit> scanSchemaByColValue(String value, String date, String fieldName);
}
