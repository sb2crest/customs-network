package com.customs.network.fdapn.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StateCodeInfoDto {
    private String countryCode;
    private JsonNode stateCodes;
}
