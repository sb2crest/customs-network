package com.customs.network.fdapn.dto;

import lombok.Data;

@Data
public class TransactionFailureResponse {
    private String batchId;
    private String uniqueUserIdentifier;
    private String referenceIdentifierNo;
    private String createdOn;
    private String status;
    private SuccessOrFailureResponse responseJson;
    private ExcelTransactionInfo requestInfo;
}
