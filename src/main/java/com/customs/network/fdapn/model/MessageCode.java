package com.customs.network.fdapn.model;

public enum MessageCode {
    SUCCESS_SUBMIT("001", "Success"),
    VALIDATION_ERRORS("002", "Validation Errors"),
    ;

    private final String code;
    private final String status;

    MessageCode(String code, String status) {
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public String getStatus() {
        return status;
    }
}
