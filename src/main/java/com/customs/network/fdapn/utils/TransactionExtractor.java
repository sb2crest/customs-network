package com.customs.network.fdapn.utils;

import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.CustomsFdapnSubmit;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class TransactionExtractor implements ResultSetExtractor<List<CustomsFdapnSubmit>> {
    @Autowired
    private final ObjectMapper objectMapper;

    public TransactionExtractor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<CustomsFdapnSubmit> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<CustomsFdapnSubmit> transactions = new ArrayList<>();
        while (rs.next()) {
            CustomsFdapnSubmit request = new CustomsFdapnSubmit();
            request.setSlNo(rs.getLong("serial"));
            request.setBatchId(rs.getString("batch_id"));
            request.setAccountId(rs.getString("account_id"));
            request.setCreatedOn(rs.getDate("created_on"));
            request.setEnvelopNumber(rs.getString("envelop_number"));
            request.setReferenceId(rs.getString("reference_id"));
            try {
                request.setRequestJson(objectMapper.readTree(rs.getString("request_json")));
                request.setResponseJson(objectMapper.readTree(rs.getString("response_json")));
            } catch (JsonProcessingException e) {
                throw new FdapnCustomExceptions(ErrorResCodes.CONVERSION_FAILURE,"Error in converting JsonB to string , "+e);
            }
            request.setStatus(rs.getString("status"));
            request.setTraceId(rs.getString("trace_id"));
            request.setUpdatedOn(rs.getDate("updated_on"));
            request.setUserId(rs.getString("user_id"));
            transactions.add(request);
        }
        return transactions;
    }
}
