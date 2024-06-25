package com.customs.network.fdapn.validations;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@AllArgsConstructor
public class DrugValidationRules {
    private String fieldName;
    private int minLength;
    private int maxLength;
    private boolean mandatory;
    private String allowedValues;

    public boolean validate(String value) {
        if (mandatory && (value == null || value.isEmpty())) {
            return false;
        }

        if (value != null && (value.length() < minLength || value.length() > maxLength)) {
            return false;
        }

        if (allowedValues != null && !allowedValues.isEmpty() && !allowedValues.contains(value)) {
            return false;
        }

        return true;
    }
}

