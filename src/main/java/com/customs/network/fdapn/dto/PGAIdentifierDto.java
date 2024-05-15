package com.customs.network.fdapn.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
public class PGAIdentifierDto {
    public JsonNode programCodeData;
//    public String governmentAgencyProgramCode;
}
