package com.customs.network.fdapn.dto;

import com.customs.network.fdapn.model.TrackingDetails;
import lombok.Data;


@Data
public class CustomerFdaPnFailure {
    private String batchId;
    private String userId;
    private String referenceIdentifierNo;
    private String createdOn;
    private String status;
    private SuccessOrFailureResponse responseJson;
    private TrackingDetails requestJson;

}
