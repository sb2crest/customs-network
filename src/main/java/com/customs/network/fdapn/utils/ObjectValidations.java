package com.customs.network.fdapn.utils;

import com.customs.network.fdapn.dto.UserProductInfoDto;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.ValidationError;
import io.micrometer.common.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.customs.network.fdapn.exception.ErrorResCodes.INVALID_DETAILS;

public class ObjectValidations {
    private ObjectValidations() {
    }

    public static void validateCustomerProductInfoDto(UserProductInfoDto dto) {
        List<ValidationError> errorList = new ArrayList<>();
        if (StringUtils.isBlank(dto.getProductCode())) {
            ValidationError validationError = new ValidationError();
            validationError.setFieldName("productCode");
            validationError.setMessage("Product code cannot be empty or null");
            validationError.setActual(dto.getProductCode());
            errorList.add(validationError);
        }
        if (StringUtils.isBlank(dto.getUniqueUserIdentifier())) {
            ValidationError validationError = new ValidationError();
            validationError.setFieldName("uniqueUserIdentifier");
            validationError.setMessage("uniqueUserIdentifier cannot be empty or null");
            validationError.setActual(dto.getUniqueUserIdentifier());
            errorList.add(validationError);
        }
        if (dto.getProductInfo().isEmpty()) {
            ValidationError validationError = new ValidationError();
            validationError.setFieldName("productInfo");
            validationError.setMessage("productInfo cannot be null");
            validationError.setActual(dto.getProductInfo());
            errorList.add(validationError);
        }
        if (!errorList.isEmpty()) {
            throw new FdapnCustomExceptions(INVALID_DETAILS, errorList);
        }
    }

}
