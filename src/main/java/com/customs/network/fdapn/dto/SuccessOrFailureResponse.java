package com.customs.network.fdapn.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class SuccessOrFailureResponse {
    private String messageCode;
    private String status;
    private JsonNode message;
    private String envelopNumber;
}
