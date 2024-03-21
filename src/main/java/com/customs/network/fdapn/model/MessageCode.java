package com.customs.network.fdapn.model;

public enum MessageCode {
    SUCCESS_SUBMIT("001", "Success"),
    VALIDATION_ERRORS("002", "Validation Errors"),
    REJECT("003","Failed"),
    PENDING("004","Pending"),
    CBP_DOWN("005","CBP Down"),
    INVALID_USER("006","Invalid User")
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
