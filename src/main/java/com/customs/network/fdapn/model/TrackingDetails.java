package com.customs.network.fdapn.model;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.util.LinkedList;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@Data
public class TrackingDetails {
    @ExcelColumn(index = 0)
    private int sNo;

    @ExcelColumn(index = 1)
    private String accountId;

    @ExcelColumn(index = 2)
    private String userId;

    @ExcelColumn(index = 3)
    private String modeOfTransportation;

    @ExcelColumn(index = 4)
    private String entryType;

    @ExcelColumn(index = 5)
    private String referenceIdentifier;

    @ExcelColumn(index = 6)
    private String referenceIdentifierNo;

    @ExcelColumn(index = 7)
    private String filer;

    @ExcelColumn(index = 8)
    private String billType;

    @ExcelColumn(index = 9)
    private String carrier;

    @ExcelColumn(index = 10)
    private String billTypeIndicator;

    @ExcelColumn(index = 11)
    private String issuerCode;

    @ExcelColumn(index = 12)
    private String billingOfLading;

    @ExcelColumn(index = 13)
    private int priorNoticeNumber;

    @ExcelColumn(index = 14)
    private String productNumber;

    @ExcelColumn(index = 15)
    private String commercialDesc;

    @ExcelColumn(index = 16)
    private String governmentAgencyProcessingCode;

    @ExcelColumn(index = 17)
    private String commodityDesc;

    @ExcelColumn(index = 18)
    private String countryOfProduction;

    @ExcelColumn(index = 19)
    private String countryOfShipment;

    @ExcelColumn(index = 20)
    private String arrivalLocation;

    @ExcelColumn(index = 21)
    private String arrivalDate;

    @ExcelColumn(index = 22)
    private String arrivalTime;

    @ExcelColumn(index = 23)
    private String packageTrackingCode;

    @ExcelColumn(index = 24)
    private String packageTrackingNumber;

    @ExcelColumn(index = 25)
    private String containerNumber;

    private LinkedList<PartyDetails> partyDetails;

    @ExcelColumn(index = 40)
    private long baseQuantity;

    @ExcelColumn(index = 41)
    private String baseUOM;

    @ExcelColumn(index = 42)
    private int packagingQualifier;

    @ExcelColumn(index = 43)
    private long quantity;

    @ExcelColumn(index = 44)
    private String UOM;

    @ExcelColumn(index = 45)
    private String affirmationComplianceCode;

    @ExcelColumn(index = 46)
    private String affirmationComplianceQualifier;

    @ExcelColumn(index = 47)
    private String end;
}
