package com.customs.network.fdapn.validations.objects;

import lombok.Data;

import java.util.List;

@Data
public class ProductDetails {

    private String governmentAgencyCode;
    private String governmentAgencyProgramCode;
    private String governmentAgencyProcessingCode;
    private String productCodeNumber;
    private String commodityDesc;
    private String productCodeQualifier;
    private String commercialDesc;
    private String intendedUseCode;
    private String intendedUseDescription;
    private String correctionIndicator;
    private String disclaimer;
    private String pgaLineNumber;
    private String remarksTypeCode;
    private String remarksText;
    private String itemType;
    private String packageTrackingCode;
    private String packageTrackingNumber;
    private List<AnticipatedArrivalInformation> anticipatedArrivalInformations;
    private List<ProductConstituentElement> productConstituentElements;
    private List<ProductOrigin> productOrigin;
    private TradeOrBrandNameInfo tradeOrBrandNameInfo;
    private List<EntityDetails> partyDetails;
    private List<List<ProductPackaging>> productPackaging;
    private List<AffirmationOfCompliance> affirmationOfComliance;
    private List<ProductCondition> productCondition;
    private List<ContainerInformation> containerInformation;

}
