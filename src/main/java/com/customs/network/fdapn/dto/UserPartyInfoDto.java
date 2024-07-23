package com.customs.network.fdapn.dto;

import com.customs.network.fdapn.model.ExcelColumn;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class UserPartyInfoDto {
    @ExcelColumn(index = 1)
    private String uniqueUserIdentifier;
    @ExcelColumn(index = 2)
    private String partyIdentifierId;

    @ExcelColumn(index = 3)
    private String actionCode;

    private List<String> partyIdentifiers;


    private JsonNode partyInfo;
    private int pageNumber;
    private int pageSize;
}
