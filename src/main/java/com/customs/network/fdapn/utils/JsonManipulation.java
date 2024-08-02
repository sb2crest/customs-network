package com.customs.network.fdapn.utils;

import com.customs.network.fdapn.dto.ProductValidationResponse;
import com.customs.network.fdapn.dto.UserProductInfoDto;
import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.service.UserProductInfoServices;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.customs.network.fdapn.utils.DefinedFieldsJson.isValidDefinedRootField;

@Component
@Slf4j
public class JsonManipulation {
    private final UserProductInfoServices userInfoServices;

    public JsonManipulation(UserProductInfoServices userInfoServices) {
        this.userInfoServices = userInfoServices;
    }

    public ProductValidationResponse processUpdateAction(UserProductInfoDto object) {
        long startTime = System.currentTimeMillis();
        JsonNode update = object.getProductInfo();
        String uniqueUserIdentifier = object.getUniqueUserIdentifier();
        String productCode = object.getProductCode();
        UserProductInfoDto originalProductInfo = userInfoServices.getProductByProductCode(uniqueUserIdentifier, productCode);
        ObjectNode original = (ObjectNode) originalProductInfo.getProductInfo();
        log.info("Original Product Info: {}", original);
        log.info("Update Product Info: {}", update);
        if (update != null && update.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = update.fields();
            List<ValidationError> validationErrorList = new ArrayList<>();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String fieldName = entry.getKey();
                if (isValidDefinedRootField(fieldName)) {
                    JsonNode newValue = entry.getValue();
                    if (newValue != null && !newValue.isNull()) { // Check if newValue is not null and not JSON null
                        if (original.has(fieldName) && !original.get(fieldName).equals(newValue)) {
                            original.set(fieldName, newValue);
                        } else if (!original.has(fieldName)) {
                            original.set(fieldName, newValue);
                            log.warn("Field '{}' not found for update. Since it is a valid key, adding the field in the Product info for user '{}' and product code '{}'",
                                    fieldName, uniqueUserIdentifier, productCode);
                        }
                    } else {
                        log.warn("Field '{}' has a null value in the update. Skipping update for this field.", fieldName);
                    }
                } else {
                    log.error("Field '{}' is not a valid field", fieldName);
                    ValidationError validationError = new ValidationError();
                    validationError.setFieldName(fieldName);
                    validationError.setMessage("Invalid field");
                    validationError.setActual("The field provided for the product " + productCode + " is not a valid field");
                    validationErrorList.add(validationError);
                }
            }
            if (validationErrorList.isEmpty()) {
                log.info("Modified Product Info: {}", original);
                originalProductInfo.setProductInfo(original);
                long endTime = System.currentTimeMillis();
                log.info("Time taken by processUpdateAction(): {} seconds", (endTime - startTime) / 1000.0);
            }
            ProductValidationResponse response = new ProductValidationResponse();
            response.setValidationErrors(validationErrorList);
            response.setUserProductInfo(originalProductInfo);
            return response;
        } else {
            log.warn("Update JSON is null or not an object. No updates applied.");
            return new ProductValidationResponse(); // Return empty response if update JSON is null or not an object
        }
    }



    public void updateRequiredJsonFields(JsonNode info, String productCode) {
        ((ObjectNode) info).put("productCodeNumber", productCode);
        ((ObjectNode) info).put("governmentAgencyCode", "FDA");
        ((ObjectNode) info).put("productCodeQualifier", "FDP");
        ((ObjectNode) info).put("remarksTypeCode", "GEN");
        traverseAndModify(info);
    }

    private void traverseAndModify(JsonNode node) {
        if (node == null || node.isNull()) {
            return;
        }

        if (node.isObject()) {
            handleObjectNode((ObjectNode) node);
        } else if (node.isArray()) {
            handleArrayNode((ArrayNode) node);
        }
    }

    private void handleObjectNode(ObjectNode objectNode) {
        if (objectNode.has("pointOfContacts")) {
            ArrayNode pointOfContacts = (ArrayNode) objectNode.get("pointOfContacts");
            if (pointOfContacts != null && pointOfContacts.isArray()) {
                pointOfContacts.forEach(pointOfContact -> {
                    if (!pointOfContact.has("additionalInformations")) {
                        ((ObjectNode) pointOfContact).putArray("additionalInformations");
                    }
                });
            }
        }
        objectNode.fields().forEachRemaining(entry -> traverseAndModify(entry.getValue()));
    }

    private void handleArrayNode(ArrayNode arrayNode) {
        arrayNode.forEach(this::traverseAndModify);
    }


}
