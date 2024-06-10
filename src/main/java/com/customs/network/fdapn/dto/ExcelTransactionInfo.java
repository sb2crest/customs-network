package com.customs.network.fdapn.dto;

import com.customs.network.fdapn.model.ExcelColumn;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class ExcelTransactionInfo {
    @ExcelColumn(index = 0)
    private int slNo;

    @ExcelColumn(index = 1)
    private String uniqueUserIdentifier;

    private String referenceId; //for internal use only

    @ExcelColumn(index = 2)
    private String actionCode;

    private PriorNoticeData priorNoticeData;

    private List<String> productCode;
}
