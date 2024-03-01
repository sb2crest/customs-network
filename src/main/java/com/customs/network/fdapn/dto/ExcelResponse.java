package com.customs.network.fdapn.dto;

import com.customs.network.fdapn.model.CustomerDetails;
import com.customs.network.fdapn.model.ValidationError;
import lombok.Data;

import java.util.List;

@Data
public class ExcelResponse {
    private CustomerDetails customerDetails;
    private List<ValidationError> validationErrors;
}
