package com.customs.network.fdapn.validations.utils;

import com.customs.network.fdapn.model.ValidationError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ErrorUtils {
    private ErrorUtils(){

    }
    public static ValidationError createValidationError(String fieldName, String message, Object actual) {
        ValidationError validationError = new ValidationError();
        validationError.setFieldName(fieldName);
        validationError.setMessage(message);
        validationError.setActual(actual);
        return validationError;
    }

    public static ValidationError createValidationError(String productCode, String fieldName, String message, Object actual) {
        ValidationError validationError = new ValidationError();
        validationError.setFieldName(fieldName);
        validationError.setMessage(message);
        validationError.setActual(actual);
        validationError.setProductCode(productCode);
        return validationError;

    }

    public static ValidationError createValidationError(String productCode, String fieldName, String message, Object actual, Object expected) {
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

    public static <T> List<ValidationError> checkInitialViolations(T obj) {
        List<ValidationError> validationErrorList = new ArrayList<>();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<T>> violations = validator.validate(obj);
        for (ConstraintViolation<T> violation : violations) {
            validationErrorList.add(createValidationError(violation.getPropertyPath().toString(),
                    violation.getMessage(),violation.getInvalidValue()));
        }
        return validationErrorList;
    }
}
