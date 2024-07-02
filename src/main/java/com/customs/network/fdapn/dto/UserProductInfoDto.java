package com.customs.network.fdapn.dto;

import com.customs.network.fdapn.model.ExcelColumn;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@AllArgsConstructor
@NoArgsConstructor
public class UserProductInfoDto {
    @ExcelColumn(index = 2)
    private String productCode;
    @ExcelColumn(index = 1)
    private String uniqueUserIdentifier;
    @JsonIgnore
    @ExcelColumn(index = 3)
    private String actionCode;
    private List<String> productCodeList;
    private JsonNode productInfo;
    private boolean isValid;
    private JsonNode validationErrors;
    private int pageNumber;
    private int pageSize;

}
