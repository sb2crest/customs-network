package com.customs.network.fdapn.validations.objects.commodity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ProductDetails {
    private String typeOfSubmission;
    private String governmentAgencyCode;
    @NotNull(message = "governmentAgencyProgramCode is mandatory")
    @Size(min = 3, max = 3,message = "governmentAgencyProgramCode must be exactly {max} characters")
    private String governmentAgencyProgramCode;
    @Size(max = 3,message = "governmentAgencyProcessingCode must be exactly {max} characters")
    private String governmentAgencyProcessingCode;
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
    @Size(min = 3 ,max = 3,message = "remarksTypeCode can have maximum {max} characters")
    private String remarksTypeCode;
    @Size(max = 68, message = "remarksText can have maximum {max} characters")
    private String remarksText;
    private String itemType;
    private String packageTrackingCode;
    private String packageTrackingNumber;

    private List<AnticipatedArrivalInformations> anticipatedArrivalInformations;
    @Valid
    private List<ProductConstituentElement> productConstituentElements;
    @NotNull(message = "productOrigin cannot be null")
    @Valid
    private List<ProductOrigin> productOrigin;
    @Valid
    private TradeOrBrandNameInfo tradeOrBrandNameInfo;
    @Valid
    private LicensePlateIssuer licensePlateIssuer;
    @Valid
    private LicensePlateNumber licensePlateNumber;
    @Valid
    private CourierTrackingAndDimensions courierTrackingAndDimensions;
    @Valid
    private List<EntityDetails> partyDetails;

    private List<List<ProductPackaging>> productPackaging;

    @Valid
    private List<AffirmationOfCompliance> affirmationOfCompliance;

    private List<ProductCondition> productCondition;

    @Valid
    private List<ContainerInformation> containerInformation;

}
