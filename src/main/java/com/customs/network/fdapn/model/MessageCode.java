package com.customs.network.fdapn.model;

public enum MessageCode {
    SUCCESS_SUBMIT("001", "PENDING"),
    VALIDATION_ERRORS("002", "VALIDATION ERROR"),
    REJECT("003","REJECTED"),
    CBP_DOWN("004","CBP DOWN"),
    INVALID_USER("005","INVALID_USER")
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
