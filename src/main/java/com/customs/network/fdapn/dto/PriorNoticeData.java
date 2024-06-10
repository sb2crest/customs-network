package com.customs.network.fdapn.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@JsonPropertyOrder({
        "uniqueUserIdentifier",
        "actionCode",
        "filingType",
        "referenceQualifierCode",
        "issuerCodeForReferenceIdentifier",
        "referenceIdentifierNo",
        "filerDefinedReferenceNo",
        "billTypeIndicator",
        "carrier",
        "entryType",
        "modeOfTransportation",
        "envelopeNumber",
        "billTypeIndicatorPE15",
        "issuerCodeOfBillOfLadingNumber",
        "billOfLadingNumber",
        "priorNoticeConfirmationNumber"
})
public class PriorNoticeData {
    private String uniqueUserIdentifier;
    private String actionCode;
    private String filingType;
    private String referenceQualifierCode;
    private String issuerCodeForReferenceIdentifier;
    private String referenceIdentifierNo;
    private String filerDefinedReferenceNo;
    private String billTypeIndicator;
    private String carrier;
    private String entryType;
    private String modeOfTransportation;
    private String envelopeNumber;
    private String billTypeIndicatorPE15;
    private String issuerCodeOfBillOfLadingNumber;
    private String billOfLadingNumber;
    private String priorNoticeConfirmationNumber;
    @JsonIgnore
    private String productCodeList;
    private List<JsonNode> products = new ArrayList<>();
}
