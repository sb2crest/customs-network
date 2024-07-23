package com.customs.network.fdapn.utils;

import com.customs.network.fdapn.dto.UserPartyInfoDto;
import com.customs.network.fdapn.dto.UserProductInfoDto;
import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.validations.objects.EntityDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import io.micrometer.common.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.customs.network.fdapn.exception.ErrorResCodes.INVALID_DETAILS;

public class ObjectValidations {
    private static final ObjectMapper mapper = new ObjectMapper();
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

    public static void validateUserPartyInfoDto(UserPartyInfoDto userPartyInfoDto) {
        String uniqueUserIdentifier = userPartyInfoDto.getUniqueUserIdentifier();
        JsonNode partyInfo = userPartyInfoDto.getPartyInfo();
        if (StringUtils.isBlank(uniqueUserIdentifier)) {
            throw new FdapnCustomExceptions(ErrorResCodes.INVALID_DETAILS, "unique user identifier is required");
        }
        if (partyInfo == null) {
            throw new FdapnCustomExceptions(ErrorResCodes.INVALID_DETAILS, "party info is required");
        }
        validatePartyInfoStructure(partyInfo);
    }

    private static void validatePartyInfoStructure(JsonNode partyInfo) {
        try {
            ObjectMapper strictMapper = mapper.copy();
            strictMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
            strictMapper.treeToValue(partyInfo, EntityDetails.class);
        } catch (JsonProcessingException e) {
            if (e instanceof UnrecognizedPropertyException unrecognizedPropertyException) {
                throw new FdapnCustomExceptions(ErrorResCodes.INVALID_DETAILS,
                        "Invalid JSON provided for partyInfo: Unknown field '" +
                                unrecognizedPropertyException.getPropertyName() + "'");
            } else {
                throw new FdapnCustomExceptions(ErrorResCodes.INVALID_DETAILS,
                        "Invalid JSON provided for partyInfo: " + e.getMessage());
            }
        }
    }

}
