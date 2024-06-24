package com.customs.network.fdapn.dto;

import com.customs.network.fdapn.model.ValidationError;
import lombok.Data;

import java.util.List;

@Data
public class ProductValidationResponse {
    private UserProductInfoDto userProductInfo;
    private List<ValidationError> validationErrors;
}
