package com.customs.network.fdapn.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProductInfoDto {
    private String productCode;
    private String userId;
    private JsonNode productInfo;
}
