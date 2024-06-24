package com.customs.network.fdapn.utils;

import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.TransactionInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
@Component
public class ExtractTransactions implements ResultSetExtractor<List<TransactionInfo>> {

    private final ObjectMapper objectMapper;

    public ExtractTransactions(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<TransactionInfo> extractData(ResultSet rs) throws SQLException, DataAccessException {
        List<TransactionInfo> transactions = new ArrayList<>();
        while (rs.next()) {
            TransactionInfo request = new TransactionInfo();
            request.setSlNo(rs.getLong("serial"));
            request.setBatchId(rs.getString("batch_id"));
            request.setCreatedOn(rs.getTimestamp("created_on"));
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
            request.setUpdatedOn(rs.getTimestamp("updated_on"));
            request.setUniqueUserIdentifier(rs.getString("unique_user_identifier"));
            transactions.add(request);
        }
        return transactions;
    }
}
