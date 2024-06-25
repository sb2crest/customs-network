package com.customs.network.fdapn.service.impl;

import com.customs.network.fdapn.dto.ExcelValidationResponse;
import com.customs.network.fdapn.dto.ProductValidationResponse;
import com.customs.network.fdapn.dto.UserProductInfoDto;
import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.service.ProductServicePreProcessor;
import com.customs.network.fdapn.service.UserProductInfoServices;
import com.customs.network.fdapn.validations.ValidateProduct;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.customs.network.fdapn.utils.DefinedFieldsJson.isValidDefinedRootField;
import static com.customs.network.fdapn.utils.JsonUtils.convertJsonToValidationErrorList;
import static com.customs.network.fdapn.utils.JsonUtils.convertValidationErrorsToJson;

@Service
@Slf4j
public class ProductServicePreProcessorImpl implements ProductServicePreProcessor {
    private final UserProductInfoServices userInfoServices;
    private final ValidateProduct validateProduct;


    public ProductServicePreProcessorImpl(UserProductInfoServices userInfoServices, ValidateProduct validateProduct) {
        this.userInfoServices = userInfoServices;
        this.validateProduct = validateProduct;
    }

    @Override
    public void processProductInfo(List<UserProductInfoDto> data) {
        data.parallelStream()
                .filter(Objects::nonNull)
                .forEach(this::performAction);
    }

    private void performAction(UserProductInfoDto object) {
        switch (object.getActionCode().toUpperCase()) {
            case "A":
                object.setValidationErrors(validate(object.getProductInfo()));
                userInfoServices.saveProduct(object);
                break;
            case "R":
                object.setValidationErrors(validate(object.getProductInfo()));
                userInfoServices.updateProductInfo(object);
                break;
            case "D":
                userInfoServices.deleteProduct(object.getUniqueUserIdentifier(), object.getProductCode());
                break;
            case "E":
                ProductValidationResponse response = processUpdateAction(object);
                UserProductInfoDto userInfo=response.getUserProductInfo();
                userInfo.setValidationErrors(validate(response.getUserProductInfo().getProductInfo()));
                if(!response.getValidationErrors().isEmpty()) {
                    List<ValidationError> existingErrors=convertJsonToValidationErrorList(userInfo.getValidationErrors());
                    existingErrors.addAll(response.getValidationErrors());
                    userInfo.setValidationErrors(convertValidationErrorsToJson(existingErrors));
                }
                userInfoServices.updateProductInfo(userInfo);
                break;
            default:
                log.error("Invalid Action code");
        }
    }
    private ProductValidationResponse processUpdateAction(UserProductInfoDto object) {
        long startTime = System.currentTimeMillis();
        JsonNode update = object.getProductInfo();
        String uniqueUserIdentifier=object.getUniqueUserIdentifier();
        String productCode=object.getProductCode();
        UserProductInfoDto originalProductInfo = userInfoServices.getProductByProductCode(uniqueUserIdentifier, productCode);
        ObjectNode original = (ObjectNode) originalProductInfo.getProductInfo();
        log.info("Original Product Info: {}", original);
        log.info("Update Product Info: {}", update);
        Iterator<Map.Entry<String, JsonNode>> fields = update.fields();
        List<ValidationError> validationErrorList=new ArrayList<>();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String fieldName = entry.getKey();
            if(isValidDefinedRootField(fieldName)){
                JsonNode newValue = entry.getValue();
                if (original.has(fieldName) && !original.get(fieldName).equals(newValue)) {
                    original.set(fieldName, newValue);
                } else if (!original.has(fieldName)) {
                    original.set(fieldName, newValue);
                    log.warn("Field ' {} , not found for update, Since it is a valid Key adding the filed in the Product info for user {} and product code  {} "
                            ,fieldName,uniqueUserIdentifier,productCode);
                }
            }else{
                log.error("Field '{}' is not a valid field", fieldName);
                ValidationError validationError=new ValidationError();
                validationError.setFieldName(fieldName);
                validationError.setMessage("Invalid field");
                validationError.setActual("The field provided for the product "+productCode+" is not a valid field");
                validationErrorList.add(validationError);
            }
        }
        if(validationErrorList.isEmpty()){
            log.info("Modified Product Info: {}", original);
            originalProductInfo.setProductInfo(original);
            long endTime = System.currentTimeMillis();
            log.info("Time taken by processUpdateAction() :->{} seconds", (endTime - startTime) / 1000.0);
        }
        ProductValidationResponse response = new ProductValidationResponse();
        response.setValidationErrors(validationErrorList);
        response.setUserProductInfo(originalProductInfo);
        return response;
    }

    private JsonNode validate(JsonNode productInfo) {
        try {
            List<ValidationError> validationErrors = validateProduct.validateProduct(productInfo);
            if (validationErrors.isEmpty()) {
                return null;
            } else {
                return convertValidationErrorsToJson(validationErrors);
            }
        } catch (JsonProcessingException e) {
            log.error("Exception while converting validation errors to json: - " + e.getMessage());
            return null;
        }
    }

}