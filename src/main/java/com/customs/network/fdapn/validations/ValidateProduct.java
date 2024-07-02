package com.customs.network.fdapn.validations;

import com.customs.network.fdapn.dto.ExcelTransactionInfo;
import com.customs.network.fdapn.dto.PriorNoticeData;
import com.customs.network.fdapn.dto.ExcelValidationResponse;
import com.customs.network.fdapn.validations.objects.ProductDetails;
import com.customs.network.fdapn.validations.productdto.Product;
import com.customs.network.fdapn.model.ValidationError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ValidateProduct {
    private final ObjectMapper objectMapper;
    private Map<String, CommodityValidator> commodityValidators;

    public ValidateProduct(ObjectMapper objectMapper, List<CommodityValidator> commodityValidatorList) {
        this.objectMapper = objectMapper;
        this.commodityValidators = commodityValidatorList.stream()
                .collect(Collectors.toMap(validation -> validation.getClass().getSimpleName().replace("CommodityValidator", "").toUpperCase(), Function.identity()));

    }

    public List<ValidationError> validateProduct(JsonNode productInfo) throws JsonProcessingException {
        List<ValidationError> errors = new ArrayList<>();
        ProductDetails product = objectMapper.treeToValue(productInfo, ProductDetails.class);
        String programCode=product.getGovernmentAgencyProgramCode();
        if(StringUtils.isNotBlank(product.getGovernmentAgencyProgramCode()) && ValidationConstants.isValidProgramCode(programCode.toUpperCase())){
           if(commodityValidators.containsKey(programCode.toUpperCase())){
               errors.addAll(checkInitialViolations(product));
               CommodityValidator validator = commodityValidators.get(programCode);
               List<ValidationError> validationErrors = validator.validate(product);
               errors.addAll(validationErrors);
             }
        }else{
            errors.add(createValidationError(product.getProductCodeNumber(),"productCodeNumber","Invalid product code number",programCode));
        }
        return new ArrayList<>();
    }

    public List<ExcelValidationResponse> validateExcelTransactions(List<ExcelTransactionInfo> transactions){
      return transactions.stream()
                .filter(Objects::nonNull)
                .map(obj->{
                    ExcelValidationResponse response = new ExcelValidationResponse();
                    response.setExcelTransactionInfo(obj);

                    List<ValidationError> validationErrorList = new ArrayList<>();
                    PriorNoticeData data=obj.getPriorNoticeData();
                    //do validation
                    response.setValidationErrorList(validationErrorList);

                    return response;

                }).toList();
    }

    private <T> List<ValidationError> checkInitialViolations(T obj){
        List<ValidationError> validationErrorList = new ArrayList<>();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<T>> violations = validator.validate(obj);
        for (ConstraintViolation<T> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            Object actual = violation.getInvalidValue();
            ValidationError validationError = new ValidationError();
            validationError.setFieldName(fieldName);
            validationError.setMessage(message);
            validationError.setActual(actual);
            validationErrorList.add(validationError);
        }
        return validationErrorList;
    }
    private ValidationError createValidationError(String fieldName, String message, Object actual) {
        ValidationError validationError = new ValidationError();
        validationError.setFieldName(fieldName);
        validationError.setMessage(message);
        validationError.setActual(actual);
        return validationError;
    }
    private ValidationError createValidationError(String productCode,String fieldName, String message, Object actual) {
        ValidationError validationError = new ValidationError();
        validationError.setFieldName(fieldName);
        validationError.setMessage(message);
        validationError.setActual(actual);
        validationError.setProductCode(productCode);
        return validationError;

    }
    private ValidationError createValidationError(String productCode,String fieldName, String message, Object actual,Object expected) {
        ValidationError validationError = new ValidationError();
        validationError.setFieldName(fieldName);
        validationError.setMessage(message);
        validationError.setActual(actual);
        validationError.setProductCode(productCode);
        validationError.setExpected(expected);
        return validationError;
    }

}
