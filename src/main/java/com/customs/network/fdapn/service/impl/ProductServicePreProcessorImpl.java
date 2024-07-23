package com.customs.network.fdapn.service.impl;

import com.customs.network.fdapn.dto.ProductValidationResponse;
import com.customs.network.fdapn.dto.UserProductInfoDto;
import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.service.ProductServicePreProcessor;
import com.customs.network.fdapn.service.UserProductInfoServices;
import com.customs.network.fdapn.utils.JsonManipulation;
import com.customs.network.fdapn.validations.ValidationEntryPoint;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.customs.network.fdapn.utils.JsonUtils.convertJsonToValidationErrorList;
import static com.customs.network.fdapn.utils.JsonUtils.convertValidationErrorsToJson;

@Service
@Slf4j
public class ProductServicePreProcessorImpl implements ProductServicePreProcessor {
    private final UserProductInfoServices userInfoServices;
    private final ValidationEntryPoint validationEntryPoint;
    private final JsonManipulation jsonManipulation;


    public ProductServicePreProcessorImpl(UserProductInfoServices userInfoServices, ValidationEntryPoint validationEntryPoint, JsonManipulation jsonManipulation) {
        this.userInfoServices = userInfoServices;
        this.validationEntryPoint = validationEntryPoint;
        this.jsonManipulation = jsonManipulation;
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
                saveAction(object);
                break;
            case "R":
                updateAction(object);
                break;
            case "D":
                deleteAction(object);
                break;
            case "E":
                UserProductInfoDto userProductInfoDto = editAction(object);
                updateAction(userProductInfoDto);
                break;
            default:
                log.error("Invalid Action code");
        }
    }

    //Actions
    @Override
    public String saveAction(UserProductInfoDto object) {
        jsonManipulation.updateRequiredJsonFields(object.getProductInfo(), object.getProductCode());
        object.setValidationErrors(validate(object.getProductInfo(), object.getProductCode()));
        return userInfoServices.saveProduct(object);
    }

    @Override
    public String updateAction(UserProductInfoDto object) {
        jsonManipulation.updateRequiredJsonFields(object.getProductInfo(), object.getProductCode());
        object.setValidationErrors(validate(object.getProductInfo(), object.getProductCode()));
        return userInfoServices.updateProductInfo(object);
    }
    @Override
    public String deleteAction(UserProductInfoDto object) {
        return userInfoServices.deleteProduct(object.getUniqueUserIdentifier(), object.getProductCode());
    }

    @Override
    public UserProductInfoDto editAction(UserProductInfoDto object) {
        ProductValidationResponse response = jsonManipulation.processUpdateAction(object);
        UserProductInfoDto userInfo = response.getUserProductInfo();
        jsonManipulation.updateRequiredJsonFields(userInfo.getProductInfo(), object.getProductCode());
        userInfo.setValidationErrors(validate(response.getUserProductInfo().getProductInfo(), object.getProductCode()));
        if (!response.getValidationErrors().isEmpty()) {
            List<ValidationError> existingErrors = convertJsonToValidationErrorList(userInfo.getValidationErrors());
            existingErrors.addAll(response.getValidationErrors());
            userInfo.setValidationErrors(convertValidationErrorsToJson(existingErrors));
        }
        return userInfo;
    }


    private JsonNode validate(JsonNode productInfo, String productCode) {
        try {
            List<ValidationError> validationErrors = validationEntryPoint.validateProduct(productInfo);
            if (validationErrors.isEmpty()) {
                return null;
            } else {
                return convertValidationErrorsToJson(validationErrors);
            }
        } catch (JsonProcessingException e) {
            log.error("Exception while converting validation errors to json: - " + e.getMessage());
            throw new FdapnCustomExceptions(ErrorResCodes.CONVERSION_FAILURE, "Please re-check the json structure provided for the product " + productCode + ", reason : " + e.getMessage());
        }
    }

}
