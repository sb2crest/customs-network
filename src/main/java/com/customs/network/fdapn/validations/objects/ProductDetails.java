package com.customs.network.fdapn.validations.objects;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ProductDetails {

    private String governmentAgencyCode;
    @NotNull(message = "governmentAgencyProgramCode is mandatory")
    @Size(min = 3, max = 3,message = "governmentAgencyProgramCode must be exactly {max} characters")
    private String governmentAgencyProgramCode;
    @NotNull(message = "governmentAgencyProcessingCode is mandatory")
    @Size(min = 3, max = 3,message = "governmentAgencyProcessingCode must be exactly {max} characters")
    private String governmentAgencyProcessingCode;
    @NotNull(message = "productCodeNumber is mandatory")
    @Size(min = 7, max = 7,message = "productCodeNumber must be exactly {max} characters")
    private String productCodeNumber;
    @NotNull(message = "commodityDesc must not be null")
    @Size(min = 1, max = 57, message = "commodityDesc must be between {min} and {max} characters")
    private String commodityDesc;
    private String productCodeQualifier;
    private String commercialDesc;
    @Size(max = 16, message = "intendedUseCode can have maximum {max} characters")
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
    @NotNull(message = "Party details must not be null")
    @Valid
    private List<EntityDetails> partyDetails;
    private List<List<ProductPackaging>> productPackaging;
    @NotNull(message = "AffirmationOfCompliance must not be null")
    @Valid
    private List<AffirmationOfCompliance> affirmationOfCompliance;
    private List<ProductCondition> productCondition;
    private List<ContainerInformation> containerInformation;

}
