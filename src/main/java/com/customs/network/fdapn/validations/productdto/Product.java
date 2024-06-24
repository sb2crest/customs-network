package com.customs.network.fdapn.validations.productdto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
    @NotNull(message = "Party details must not be null")
    @Valid
    private List<PartyDetails> partyDetails;
    private List<ProductPackaging> productPackaging;
    @NotNull(message = "commodityDesc must not be null")
    @Size(min = 1, max = 57, message = "commodityDesc must be between {min} and {max} characters")
    private String commodityDesc;
    private String productCodeNumber;
    private String productCodeQualifier;
    private String commercialDesc;
    private String countryOfShipment;
    private String packageTrackingCode;
    private String packageTrackingNumber;
    @Size(max = 16, message = "intendedUseCode can have maximum {max} characters")
    private String intendedUseCode;
    private String intendedUseDescription;
    private String correctionIndicator;
    private String disclaimer;
    @NotNull(message = "AffirmationOfCompliance must not be null")
    @Valid
    private List<AffirmationOfCompliance> affirmationOfCompliance;
    private List<ProductCondition> productCondition;
    private List<ContainerInformation> containerInformation;
    private List<AdditionalInformation> additionalInformation;
    private String pgaLineNumber;
    private String remarksTypeCode;
    private String remarksText;
    private String governmentAgencyCode;
    @NotNull(message = "governmentAgencyProgramCode is mandatory")
    @Size(min = 3, max = 3,message = "governmentAgencyProgramCode must be exactly {max} characters")
    private String governmentAgencyProgramCode;
    @NotNull(message = "governmentAgencyProcessingCode is mandatory")
    @Size(min = 3, max = 3,message = "governmentAgencyProcessingCode must be exactly {max} characters")
    private String governmentAgencyProcessingCode;
    private String itemType;
}
