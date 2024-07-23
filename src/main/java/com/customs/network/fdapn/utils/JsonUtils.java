package com.customs.network.fdapn.utils;

import com.customs.network.fdapn.dto.ExcelTransactionInfo;
import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.validations.objects.EntityDetails;
import com.fasterxml.jackson.annotation.JsonInclude;
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

@Component
@Slf4j
public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private JsonUtils() {
    }

    public static ExcelTransactionInfo convertJsonNodeToExcelResponseInfo(JsonNode jsonNode) {
        try {
            return objectMapper.treeToValue(jsonNode, ExcelTransactionInfo.class);
        } catch (JsonProcessingException e) {
            throw new FdapnCustomExceptions(ErrorResCodes.CONVERSION_FAILURE, "Error converting JsonNode to CustomerDetails , " + e);
        }
    }

    public static EntityDetails convertJsonNodeToEntityDetails(JsonNode jsonNode) {
        try {
            return objectMapper.treeToValue(jsonNode, EntityDetails.class);
        } catch (JsonProcessingException e) {
            throw new FdapnCustomExceptions(ErrorResCodes.CONVERSION_FAILURE, "Error converting JsonNode to CustomerDetails , " + e);
        }
    }

    public static <T> JsonNode convertObjectToJson(T response) {
        try {
            ObjectMapper copyMapper = objectMapper.copy();
            copyMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            return copyMapper.valueToTree(response);
        } catch (Exception e) {
            throw new FdapnCustomExceptions(ErrorResCodes.CONVERSION_FAILURE, "Error converting SuccessOrFailureResponse to JsonNode ," + e);
        }
    }

    public static List<ValidationError> convertJsonToValidationErrorList(JsonNode validationErrorNode) {
        if (validationErrorNode == null) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(
                    validationErrorNode.traverse(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ValidationError.class)
            );

        } catch (IOException e) {
            throw new FdapnCustomExceptions(ErrorResCodes.CONVERSION_FAILURE, e.getMessage());
        }
    }

    public static JsonNode convertValidationErrorsToJson(List<ValidationError> validationErrors) {
        return objectMapper.valueToTree(validationErrors.stream()
                .map(JsonUtils::convertToJsonObject)
                .toArray());
    }

    public static List<JsonNode> convertProductInfoObjectArrayToList(List<Object[]> productInfoList) {
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