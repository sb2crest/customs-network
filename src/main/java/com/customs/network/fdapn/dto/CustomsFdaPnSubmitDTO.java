package com.customs.network.fdapn.dto;

import com.customs.network.fdapn.model.CustomerDetails;
import lombok.Data;

@Data
public class CustomsFdaPnSubmitDTO {
    private String batchId;
    private String traceId;
    private String userId;
    private String accountId;
    private String referenceId;
    private String envelopNumber;
    private String createdOn;
    private String updatedOn;
    private String status;
    private CustomerDetails responseJson;
}