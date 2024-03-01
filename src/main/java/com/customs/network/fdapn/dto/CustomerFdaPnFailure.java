package com.customs.network.fdapn.dto;

import com.customs.network.fdapn.model.CustomerDetails;
import com.customs.network.fdapn.model.ValidationError;
import lombok.Data;

import java.util.List;

@Data
public class CustomerFdaPnFailure {
    private String batchId;
    private String userId;
    private String referenceIdentifierNo;
    private String createdOn;
    private String status;
    private List<ValidationError> responseJson;
    private CustomerDetails requestJson;

}
