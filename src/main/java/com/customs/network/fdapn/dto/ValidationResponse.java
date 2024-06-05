package com.customs.network.fdapn.dto;

import com.customs.network.fdapn.model.ValidationError;
import lombok.Data;

import java.util.List;

@Data
public class ValidationResponse {
    private ExcelTransactionInfo excelTransactionInfo;
    private List<ValidationError> validationErrorList;
}
