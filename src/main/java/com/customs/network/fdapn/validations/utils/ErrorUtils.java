package com.customs.network.fdapn.validations.utils;

import com.customs.network.fdapn.model.ValidationError;

public class ErrorUtils {
    public static ValidationError createValidationError(String fieldName, String message, Object actual) {
        ValidationError validationError = new ValidationError();
        validationError.setFieldName(fieldName);
        validationError.setMessage(message);
        validationError.setActual(actual);
        return validationError;
    }
    public static ValidationError createValidationError(String productCode,String fieldName, String message, Object actual) {
        ValidationError validationError = new ValidationError();
        validationError.setFieldName(fieldName);
        validationError.setMessage(message);
        validationError.setActual(actual);
        validationError.setProductCode(productCode);
        return validationError;

    }
    public static ValidationError createValidationError(String productCode,String fieldName, String message, Object actual,Object expected) {
        ValidationError validationError = new ValidationError();
        validationError.setFieldName(fieldName);
        validationError.setMessage(message);
        validationError.setActual(actual);
        validationError.setProductCode(productCode);
        validationError.setExpected(expected);
        return validationError;
    }
    public static ValidationError createValidationError(String fieldName, String errorMessage, Object fieldValue, Object expectedValue) {
        ValidationError validationError = new ValidationError();
        validationError.setFieldName(fieldName);
        validationError.setMessage(errorMessage);
        validationError.setActual(fieldValue);
        validationError.setExpected(expectedValue);
        return validationError;
    }
}
