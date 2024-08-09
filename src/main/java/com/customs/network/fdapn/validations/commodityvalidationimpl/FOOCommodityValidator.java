package com.customs.network.fdapn.validations.commodityvalidationimpl;

import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.validations.CommodityValidator;
import com.customs.network.fdapn.validations.DataViolationMessages;
import com.customs.network.fdapn.validations.constants.ConditionalValidator;
import com.customs.network.fdapn.validations.constants.FOOCommodityConstants;
import com.customs.network.fdapn.validations.constants.ProductCodeValidator;
import com.customs.network.fdapn.validations.objects.commodity.*;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.customs.network.fdapn.utils.UtilMethods.*;
import static com.customs.network.fdapn.validations.utils.ErrorUtils.createValidationError;

@Component
@Slf4j
public class FOOCommodityValidator extends CommonValidations implements CommodityValidator, SegmentValidator {
    private final ConditionalValidator conditionalValidator;
    private final ProductCodeValidator productCodeValidator;
    private static final String PROGRAMME_CODE = "FOO";

    public FOOCommodityValidator(FOOCommodityConstants fooCommodityConstants) {
        this.conditionalValidator = fooCommodityConstants;
        this.productCodeValidator = fooCommodityConstants;
    }

    @Override
    public List<ValidationError> validate(ProductDetails productDetails) {
        List<ValidationError> errors = new ArrayList<>();
        String productCode = productDetails.getProductCodeNumber();
        ValidationContext context = new ValidationContext(productCode, errors, conditionalValidator, PROGRAMME_CODE, productDetails, null);
        validatePGAIdentifier(context);
        validateProductIdentifier(context);
        validateProductOrigin(context);
        validateProductTradeNames(context);
        validatePartyDetails(context);
        validateAffirmationOfCompliance(context);
        validateProductCondition(context);
        validateProductPackaging(context);
        return errors;
    }

    @Override
    public Set<String> validatePartyDetails(ValidationContext context) {
        Set<String> seenPartyTypes = super.validatePartyDetails(context);
        String processingCode = context.productDetails().getGovernmentAgencyProcessingCode();
        if (StringUtils.isNotBlank(processingCode)) {
            Set<String> conditionalPartyTypes = context.conditionalValidator().getConditionalPartyTypes(processingCode);
            if (!conditionalPartyTypes.isEmpty()) {
                boolean hasConditionalPartyType = conditionalPartyTypes.stream()
                        .anyMatch(seenPartyTypes::contains);
                if (!hasConditionalPartyType) {
                    context.errors().add(createValidationError(context.productCode(), "partyType",
                            "At least one party type should be present in the product condition for processing code " + processingCode,
                            seenPartyTypes.toString(), conditionalPartyTypes.toString()));
                }
            }
        }
        return seenPartyTypes;
    }

    //validate affirmation of Compliance (It doesn't check for the conditions of AOC use cases, whereas other commodities are checked. Add in the future)
    @Override
    public Set<String> validateAffirmationOfCompliance(ValidationContext context) {
        List<AffirmationOfCompliance> affirmationOfCompliance = context.productDetails().getAffirmationOfCompliance();
        Set<String> seenAocCodes = new HashSet<>();
        if (!isNullOrEmptyCollection(affirmationOfCompliance)) {
            for (AffirmationOfCompliance compliance : affirmationOfCompliance) {
                String aocCode = compliance.getAffirmationComplianceCode();
                String aocq = compliance.getAffirmationComplianceQualifier();
                boolean isValidAoc = validateIndividualAffirmation(aocCode, aocq, context);
                if (isValidAoc && !seenAocCodes.add(aocCode) && !context.conditionalValidator().isRepeatableAoc(aocCode)) {
                    context.errors().add(createValidationError(context.productCode(), "affirmationComplianceCode",
                            "Affirmation of Compliance Code " + aocCode + " is repeated", aocCode));
                }
            }
        }
        return seenAocCodes;
    }

    private boolean validateIndividualAffirmation(String aocCode, String aocq, ValidationContext context) {
        boolean isValidAoc = false;
        if (StringUtils.isNotBlank(aocCode) && !context.conditionalValidator().isValidAOCCode(aocCode)) {
            context.errors().add(createValidationError(context.productCode(), "affirmationComplianceCode",
                    "Invalid affirmation compliance code provided ", aocCode));
        } else {
            isValidAoc = true;
            String syntax = context.conditionalValidator().getAOCQSynatx(aocCode);
            if (syntax != null) {
                if (StringUtils.isNotBlank(aocq) && !aocq.matches(syntax)) {
                    String programCode = context.programCode();
                    context.errors().add(createValidationError(context.productCode(), "affirmationComplianceQualifier - " + aocCode, DataViolationMessages.getAOCQSyntaxErrorMessage(programCode, aocCode), aocq));
                } else if (StringUtils.isBlank(aocq)) {
                    context.errors().add(createValidationError(context.productCode(), "affirmationComplianceQualifier",
                            "Affirmation of Compliance Qualifier is required for the affirmationOfComplianceCode " + aocCode, null, null));
                }
            }
        }
        return isValidAoc;
    }

    //product condition additional validations ----------------------------------------------------------------
    @Override
    public void validateProductCondition(ValidationContext context) {
        List<ProductCondition> productConditions = context.productDetails().getProductCondition();
        if (!isNullOrEmptyCollection(productConditions)) {
            ProductCondition firstCondition = productConditions.get(0);
            validateIndividualProductCondition(firstCondition, context, true);
            productConditions.remove(0);
            for (ProductCondition productCondition : productConditions) {
                validateIndividualProductCondition(productCondition, context, false);
            }
        }
    }

    private void validateIndividualProductCondition(ProductCondition condition, ValidationContext context, boolean isFirst) {
        String temperatureQualifier = condition.getTemperatureQualifier();
        String lotNumberQualifier = condition.getLotNumberQualifier();
        String lotNumber = condition.getLotNumber();
        String pgaLineValue = condition.getPgaLineValue();
        String processingCode = context.productDetails().getGovernmentAgencyProcessingCode();
        if (isFirst && !isTwoParameterizedFunctionTestPassed(context.conditionalValidator()::isValidLotNumberQualifier, lotNumberQualifier, processingCode, false)) {
            context.errors().add(createValidationError(context.productCode(), "lotNumberQualifier",
                    "Invalid lot number qualifier provided", lotNumberQualifier, null));
        } else if (!isFirst) {
            if (StringUtils.isNotBlank(lotNumberQualifier)) {
                context.errors().add(createValidationError(context.productCode(), "lotNumberQualifier", "lotNumberQualifier is allowed only in the first productCondition", null));
            }
            if (StringUtils.isNotBlank(pgaLineValue)) {
                context.errors().add(createValidationError(context.productCode(), "pgaLineValue", "PGA Line Value is allowed only in the first productCondition", null));
            }
        }
        if (!isSingleParameterizedFunctionTestPassed(context.conditionalValidator()::isValidTemperatureQualifierCode, temperatureQualifier, false)) {
            context.errors().add(createValidationError(context.productCode(), "temperatureQualifier",
                    "Invalid temperature qualifier provided", temperatureQualifier, null));
        }
        if (isRequiredLotNumber(context.productCode()) && StringUtils.isBlank(lotNumber)) {
            context.errors().add(createValidationError(context.productCode(), "lotNumber",
                    "Lot Number is required for the product condition", null, null));
        }
    }

    private boolean isRequiredLotNumber(String productCode) {
        return productCodeValidator.isAFProduct(productCode) ||
                productCodeValidator.isLACFProduct(productCode) ||
                productCodeValidator.isInfantFormula(productCode);
    }

    //validateCourierTrackingAndDimensions ----------------------------------------------------------------
    @Override
    public void validateCourierTrackingAndDimensions(CommonValidations.ValidationContext context) {
        CourierTrackingAndDimensions trackingAndDimensions = context.productDetails().getCourierTrackingAndDimensions();
        if (!isNullObject(trackingAndDimensions)) {
            boolean isDimensionsAreAllowed = isDimensionsAreAllowed(context.productCode());
            String containerDimensionOne = trackingAndDimensions.getContainerDimensionsOne();
            String containerDimensionsTwo = trackingAndDimensions.getContainerDimensionsTwo();
            String containerDimensionsThree = trackingAndDimensions.getContainerDimensionsOne();
            if (!isDimensionsAreAllowed && !areAllBlank(containerDimensionOne, containerDimensionsTwo, containerDimensionsThree)) {
                context.errors().add(createValidationError(context.productCode(), "containerDimensions", "Container dimensions are not allowed for LACF or AF ", null));
            }
        }
    }

    private boolean isDimensionsAreAllowed(String productCode) {
        return productCodeValidator.isAFProduct(productCode) ||
                productCodeValidator.isLACFProduct(productCode);
    }
    //validate anticipated arrival information ----------------------------------------------------------------

    @Override
    public void validateAnticipatedArrivalLocation(ValidationContext context) {
        List<AnticipatedArrivalInformations> anticipatedArrivalInformations = context.productDetails().getAnticipatedArrivalInformations();
        if (isNullOrEmptyCollection(anticipatedArrivalInformations)) {
            context.errors().add(createValidationError(context.productCode(), "anticipatedArrivalInformations", "anticipatedArrivalInformations is mandatory", anticipatedArrivalInformations));
        } else {
            Set<String> mandatoryArrivalInformations = new HashSet<>(conditionalValidator.getMandatoryAnticipatedArrivalInformation(false));
            if (!context.conditionalValidator().isRepeatableAnticipatedArrivalLocation() && anticipatedArrivalInformations.size() > 1) {
                context.errors().add(createValidationError(context.productCode(), "anticipatedArrivalInformations", String.format("anticipatedArrivalInformations are not repeatable, repeated %d times", anticipatedArrivalInformations.size())));
            } else {
                for (AnticipatedArrivalInformations anticipatedArrivalInformation : anticipatedArrivalInformations) {
                    String validatedAAI = validateAnticipatedArrivalInformation(anticipatedArrivalInformation, context, mandatoryArrivalInformations);
                    if(validatedAAI != null)
                        mandatoryArrivalInformations.remove(validatedAAI.toUpperCase());
                }
            }
            if (!mandatoryArrivalInformations.isEmpty()) {
                context.errors().add(createValidationError(context.productCode(), "anticipatedArrivalInformations", "Missing mandatory anticipatedArrivalInformations " + mandatoryArrivalInformations.toString(), mandatoryArrivalInformations.toString()));
            }
        }

    }

    private String validateAnticipatedArrivalInformation(AnticipatedArrivalInformations anticipatedArrivalInformations, ValidationContext context, Set<String> mandatory) {
        String anticipatedArrivalDate = anticipatedArrivalInformations.getAnticipatedArrivalDate();
        String anticipatedArrivalTime = anticipatedArrivalInformations.getAnticipatedArrivalTime();
        String inspectionOrArrivalLocation = anticipatedArrivalInformations.getInspectionOrArrivalLocation();
        String anticipatedArrivalInformation = anticipatedArrivalInformations.getAnticipatedArrivalInformation();
        String inspectionOrArrivalLocationCode = anticipatedArrivalInformations.getInspectionOrArrivalLocationCode();
        if (!areAllNotBlank(anticipatedArrivalDate, anticipatedArrivalTime, inspectionOrArrivalLocation, anticipatedArrivalInformation, inspectionOrArrivalLocationCode)) {
            context.errors().add(createValidationError(context.productCode(), "anticipatedArrivalInformations", "All fields are mandatory for the commodity " + context.programCode(), anticipatedArrivalInformations.toString()));
        } else {
            if (!mandatory.contains(anticipatedArrivalInformation)) {
                context.errors().add(createValidationError(context.productCode(), "anticipatedArrivalInformations", "Invalid anticipatedArrival information ", anticipatedArrivalInformation, mandatory.toString()));
            }
        }
        return anticipatedArrivalInformation;
    }

    @Override
    public ConditionalValidator getConditionalValidator() {
        return conditionalValidator;
    }

    @Override
    public void validateProductConstituentElement(ValidationContext context) {
        //no need to validate
    }
}
