package com.customs.network.fdapn.utils;

import com.customs.network.fdapn.dto.UserProductInfoDto;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.ValidationError;
import io.micrometer.common.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.customs.network.fdapn.exception.ErrorResCodes.INVALID_DETAILS;

public class ObjectValidations {
    public static void validateCustomerProductInfoDto(UserProductInfoDto dto) {
        List<ValidationError> errorList = new ArrayList<>();
        if (StringUtils.isBlank(dto.getProductCode())) {
            ValidationError validationError = new ValidationError("productCode",
                    "Product code cannot be empty or null", dto.getProductCode());
            errorList.add(validationError);
        }
        if (StringUtils.isBlank(dto.getUniqueUserIdentifier())) {
            ValidationError validationError = new ValidationError("uniqueUserIdentifier",
                    "uniqueUserIdentifier cannot be empty or null", dto.getUniqueUserIdentifier());
            errorList.add(validationError);
        }
        if (dto.getProductInfo().isEmpty()) {
            ValidationError validationError = new ValidationError("productInfo",
                    "productInfo cannot be null", null);
            errorList.add(validationError);
        }
        if (!errorList.isEmpty()) {
            throw new FdapnCustomExceptions(INVALID_DETAILS, errorList);
        }
    }

}
