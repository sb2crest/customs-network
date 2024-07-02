package com.customs.network.fdapn.utils;

import com.customs.network.fdapn.dto.ExcelTransactionInfo;
import com.customs.network.fdapn.dto.SuccessOrFailureResponse;
import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.ValidationError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private JsonUtils() {
    }

    public static JsonNode convertExcelResponse(ExcelTransactionInfo excelTransactionInfo) {
        try {
            return objectMapper.valueToTree(excelTransactionInfo);
        } catch (Exception e) {
            throw new FdapnCustomExceptions(ErrorResCodes.CONVERSION_FAILURE,"Error converting CustomerDetails to JsonNode, "+e);
        }
    }
    public static ExcelTransactionInfo convertJsonNodeToExcelResponseInfo(JsonNode jsonNode) {
        try {
            return objectMapper.treeToValue(jsonNode, ExcelTransactionInfo.class);
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
    public static List<ValidationError> convertJsonToValidationErrorList(JsonNode validationErrorNode){
        if(validationErrorNode==null){
            return new ArrayList<>();
        }
        try{
            return objectMapper.readValue(
                    validationErrorNode.traverse(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ValidationError.class)
            );

        } catch (IOException e) {
            throw new FdapnCustomExceptions(ErrorResCodes.CONVERSION_FAILURE,e.getMessage());
        }
    }
    public static JsonNode convertValidationErrorsToJson(List<ValidationError> validationErrors) {
        return objectMapper.valueToTree(validationErrors.stream()
                .map(JsonUtils::convertToJsonObject)
                .toArray());
    }
    public static List<JsonNode> convertProductInfoObjectArrayToList(List<Object[]> productInfoList){
       return productInfoList.stream()
                .map(row -> objectMapper.convertValue(row[0], JsonNode.class))
                .toList();
    }

    private static ObjectNode convertToJsonObject(ValidationError error) {
        ObjectNode node = objectMapper.createObjectNode();
        Class<? extends ValidationError> clazz = error.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value = field.get(error);
                if (value != null) {
                    node.set(field.getName(), objectMapper.valueToTree(value));
                }
            } catch (IllegalAccessException e) {
                log.error(e.getMessage());
            }
        }
        return node;
    }

}