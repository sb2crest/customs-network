package com.customs.network.fdapn.utils;

import com.customs.network.fdapn.dto.ExcelTransactionInfo;
import com.customs.network.fdapn.dto.SuccessOrFailureResponse;
import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.TrackingDetails;
import com.customs.network.fdapn.model.ValidationError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static JsonNode convertCustomerDetailsToJson(TrackingDetails trackingDetails) {
        try {
            return objectMapper.valueToTree(trackingDetails);
        } catch (Exception e) {
            throw new FdapnCustomExceptions(ErrorResCodes.CONVERSION_FAILURE,"Error converting CustomerDetails to JsonNode, "+e);
        }
    }
    public static JsonNode convertExcelResponse(ExcelTransactionInfo excelTransactionInfo) {
        try {
            return objectMapper.valueToTree(excelTransactionInfo);
        } catch (Exception e) {
            throw new FdapnCustomExceptions(ErrorResCodes.CONVERSION_FAILURE,"Error converting CustomerDetails to JsonNode, "+e);
        }
    }
    public static TrackingDetails convertJsonNodeToCustomerDetails(JsonNode jsonNode) {
        try {
            return objectMapper.treeToValue(jsonNode, TrackingDetails.class);
        } catch (JsonProcessingException e) {
            throw new FdapnCustomExceptions(ErrorResCodes.CONVERSION_FAILURE,"Error converting JsonNode to CustomerDetails , "+e);
        }
    }
    public static JsonNode convertValidationErrorListToJson(List<ValidationError> validationErrors) {
        try {
            return objectMapper.valueToTree(validationErrors);
        } catch (Exception e) {
            throw new FdapnCustomExceptions(ErrorResCodes.CONVERSION_FAILURE,"Error converting List<ValidationError> to JsonNode "+e);
        }
    }
    public static JsonNode convertResponseToJson(SuccessOrFailureResponse response) {
        try {
            return objectMapper.valueToTree(response);
        } catch (Exception e) {
            throw new FdapnCustomExceptions(ErrorResCodes.CONVERSION_FAILURE,"Error converting SuccessOrFailureResponse to JsonNode ,"+e);
        }
    }

}