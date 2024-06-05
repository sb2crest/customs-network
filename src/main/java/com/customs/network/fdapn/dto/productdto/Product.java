package com.customs.network.fdapn.dto.productdto;

import lombok.Data;

import java.util.List;

@Data
public class Product {
    private String baseUOM;
    private long baseQuantity;
    private List<AnticipatedArrivalInformations> anticipatedArrivalInformations;
    private List<ProductConstituentElement> productConstituentElements;
    private List<ProductOrigin> productOrigin;
    private String tradeOrBrandName;
    private List<PartyDetails> partyDetails;
    private List<ProductPackaging> productPackaging;
    private String commodityDesc;
    private String productCodeNumber;
    private String productCodeQualifier;
    private String commercialDesc;
    private String countryOfShipment;
    private String packageTrackingCode;
    private String packageTrackingNumber;
    private String intendedUseCode;
    private String intendedUseDescription;
    private String correctionIndicator;
    private String disclaimer;
    private List<AffirmationOfCompliance> affirmationOfCompliance;
    private List<ProductCondition> productCondition;
    private List<ContainerInformation> containerInformation;
    private List<AdditionalInformation> additionalInformation;
    private String pgaLineNumber;
    private String remarksTypeCode;
    private String remarksText;
    private String governmentAgencyCode;
    private String governmentAgencyProgramCode;
    private String governmentAgencyProcessingCode;
    private String itemType;
}
