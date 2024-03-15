package com.customs.network.fdapn.dto;

import com.customs.network.fdapn.model.TrackingDetails;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class CustomsFdaPnSubmitDTO {
    private Long sNo;
    private String batchId;
    private String traceId;
    private String userId;
    private String accountId;
    private String referenceId;
    private String envelopNumber;
    private String createdOn;
    private String updatedOn;
    private String status;
    private TrackingDetails requestJson;
    private JsonNode responseJson;
}
