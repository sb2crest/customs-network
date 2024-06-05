package com.customs.network.fdapn.dto;

import com.customs.network.fdapn.model.ExcelColumn;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class PriorNoticeData {
    @ExcelColumn(index = 0)
    private int slNo;

    @ExcelColumn(index = 1)
    private String uniqueUserIdentifier;

    @ExcelColumn(index = 2)
    private String actionCode;

    @ExcelColumn(index = 3)
    private String filingType;

    @ExcelColumn(index = 4)
    private String referenceQualifierCode;

    @ExcelColumn(index = 5)
    private String issuerCodeForReferenceIdentifier;

    @ExcelColumn(index = 6)
    private String referenceIdentifierNo;

    @ExcelColumn(index = 7)
    private String filerDefinedReferenceNo;

    @ExcelColumn(index = 8)
    private String billTypeIndicator;

    @ExcelColumn(index = 9)
    private String carrier;

    @ExcelColumn(index = 10)
    private String entryType;

    @ExcelColumn(index = 11)
    private String modeOfTransportation;

    @ExcelColumn(index = 12)
    private String envelopeNumber;

    @ExcelColumn(index = 13)
    private String billTypeIndicatorPE15;

    @ExcelColumn(index = 14)
    private String issuerCodeOfBillOfLadingNumber;

    @ExcelColumn(index = 15)
    private String billOfLadingNumber;

    @ExcelColumn(index = 16)
    private String priorNoticeConfirmationNumber;

    @ExcelColumn(index = 17)
    @JsonIgnore
    private String productCodeList;

    private List<JsonNode> products = new ArrayList<>();
}
