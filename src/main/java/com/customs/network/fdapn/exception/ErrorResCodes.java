package com.customs.network.fdapn.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorResCodes{
    INVALID_REFERENCE_ID("1000", "Invalid Reference id", HttpStatus.BAD_REQUEST),
    NOT_FOUND("1001", "Not found", HttpStatus.NOT_FOUND),
    RECORD_NOT_FOUND("1002", "Record not found", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("1003", "User not found", HttpStatus.NOT_FOUND),
    SOMETHING_WENT_WRONG("1004", "Unexpected Error", HttpStatus.INTERNAL_SERVER_ERROR),
    EMPTY_DETAILS("1005", "Empty details", HttpStatus.BAD_REQUEST),
    INVALID_DETAILS("1006", "Invalid details", HttpStatus.BAD_REQUEST),
    CONVERSION_FAILURE("1007", "Data conversion failure", HttpStatus.INTERNAL_SERVER_ERROR),
    EMPTY_NOTIFICATION_EMAIL_LIST("1008", "Empty notification email list", HttpStatus.BAD_REQUEST),
    SERVER_ERROR("1009", "Server error", HttpStatus.INTERNAL_SERVER_ERROR),
    SERVICE_UNAVAILABLE("1010", "Service Unavailable: FDA API is currently unavailable", HttpStatus.SERVICE_UNAVAILABLE),
    ALREADY_EXISTING("1011", "Already Existing", HttpStatus.CONFLICT),
    UNKNOWN_ACTION("1013", "Unknown Action", HttpStatus.CONFLICT),
    UNSUPPORTED_FILE("1014", "Unsupported File", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    INTERNAL_SERVER_ERROR("1012", "Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String errorCode;
    private final String errorMsg;
    private final HttpStatus httpStatus;

    ErrorResCodes(String errorCode, String errorMsg, HttpStatus httpStatus) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.httpStatus = httpStatus;
    }
}
