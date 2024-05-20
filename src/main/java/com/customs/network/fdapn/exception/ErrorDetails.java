package com.customs.network.fdapn.exception;

import com.customs.network.fdapn.model.ValidationError;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDetails {
    private String errorCode;
    private String errorDesc;
    private String errorCause;
    private List<ValidationError> errorList;
}
