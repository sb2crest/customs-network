package com.customs.network.fdapn.exception;

import lombok.Data;

@Data
public class ErrorDetails {
    private String errorCode;
    private String errorDesc;
    private String errorCause;
}
