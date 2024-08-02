package com.customs.network.fdapn.validations.objects.priornotice;

import com.customs.network.fdapn.validations.annotations.Regex;
import com.customs.network.fdapn.validations.enums.RegexType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
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
        "billOfLadings"
})
public class Declaration {
    private String uniqueUserIdentifier;

    @NotNull(message = "actionCode must not be null")
    @Size(min = 1 , max = 1, message = "actionCode must be exactly {max} characters long")
    @Regex(value = RegexType.ALPHABETIC,message = "actionCode must be an alphabetic character")
    private String actionCode;

    @NotNull(message = "filingType must not be null")
    @Size(min = 1 , max = 1, message = "filingType must be exactly {max} characters long")
    @Regex(value = RegexType.ALPHANUMERIC,message = "filingType must be an alphabetic/numeric character")
    private String filingType;

    @NotNull(message = "referenceQualifierCode must not be null")
    @Size(min = 1 , max = 3, message = "referenceQualifierCode must be maximum {max} characters long")
    @Regex(value = RegexType.ALPHANUMERIC,message = "referenceQualifierCode must be an alpha-numeric")
    private String referenceQualifierCode;

    @Size(max = 4,message = "issuerCodeForReferenceIdentifier maximum {max} character long")
    @Regex(value = RegexType.ALPHANUMERIC,message = "issuerCodeForReferenceIdentifier must be an alpha-numeric")
    private String issuerCodeForReferenceIdentifier;

    @NotNull(message = "referenceIdentifierNo must not be null")
    @Size(max = 36 , message = "referenceIdentifierNo maximum {max} character long")
    private String referenceIdentifierNo;

    @Size(max = 9 , message = "filerDefinedReferenceNo maximum {max} character long")
    private String filerDefinedReferenceNo;

    @Size(min = 1 , max = 1, message = "billTypeIndicator must be exactly {max} characters long")
    private String billTypeIndicator;

    @Size(max = 4,message = "carrier maximum {max} character long")
    @Regex(value = RegexType.ALPHANUMERIC,message = "carrier must be an alpha-numeric")
    private String carrier;

    @Size(max = 2,message = "entryType maximum {max} character long")
    @Regex(value = RegexType.ALPHANUMERIC,message = "entryType must be an alpha-numeric")
    private String entryType;

    @Size(max = 2,message = "modeOfTransportation maximum {max} character long")
    @Regex(value = RegexType.ALPHANUMERIC,message = "modeOfTransportation must be an alpha-numeric")
    private String modeOfTransportation;

    @Size(max = 12,message = "envelopeNumber maximum {max} character long")
    private String envelopeNumber;

    @Valid
    private List<BillOfLading> billOfLadings;

    //additional fields for internal usage and not related to the declaration but used to transmit data through edi

    @JsonIgnore
    private String productCodeList;
    private List<JsonNode> products = new ArrayList<>();
}
