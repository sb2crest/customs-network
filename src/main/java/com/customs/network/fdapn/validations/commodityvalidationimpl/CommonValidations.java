package com.customs.network.fdapn.validations.commodityvalidationimpl;

import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.validations.DataViolationMessages;
import com.customs.network.fdapn.validations.constants.ConditionalValidator;
import com.customs.network.fdapn.validations.constants.DeclarationRules;
import com.customs.network.fdapn.validations.constants.DeclarationRulesImpl;
import com.customs.network.fdapn.validations.objects.commodity.*;
import com.customs.network.fdapn.validations.objects.priornotice.Declaration;
import io.micrometer.common.util.StringUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import java.util.*;
import java.util.function.Predicate;

import static com.customs.network.fdapn.utils.UtilMethods.isNullOrEmptyCollection;
import static com.customs.network.fdapn.validations.DataViolationMessages.getPartyIdentifierNumberErrorMessage;
import static com.customs.network.fdapn.validations.utils.ErrorUtils.checkInitialViolations;
import static com.customs.network.fdapn.validations.utils.ErrorUtils.createValidationError;

@Slf4j
public class CommonValidations {
    private final DeclarationRules declarationRules;

    public CommonValidations() {
        this.declarationRules = new DeclarationRulesImpl();
    }

    public record ValidationContext(String productCode, List<ValidationError> errors,
                                    ConditionalValidator conditionalValidator, String programCode,
                                    ProductDetails productDetails,Declaration declaration
    ) {
    }

    public void validateRequiredFields(ValidationContext context) {
        String processingCode = context.productDetails.getGovernmentAgencyProcessingCode();
        String intendedUseCode = context.productDetails.getIntendedUseCode();
        if (StringUtils.isBlank(processingCode)) {
            context.errors.add(createValidationError(context.productCode, "governmentAgencyProcessingCode",
                    String.format("Government Agency Processing Code is required for commodity %s", context.programCode), null));
        }
        if (intendedUseCode == null) {
            context.errors.add(createValidationError(context.productCode, "intendedUseCode",
                    String.format("Intended Use Code is required for commodity %s", context.programCode), null));
        }
    }

    //pga identifier validation----------------------------------------------------------------
    public void validatePGAIdentifier(ValidationContext context) {
        String productCode = context.productDetails.getProductCodeNumber();
        String disclaimer = context.productDetails.getDisclaimer();
        if (StringUtils.isBlank(disclaimer)) {
            ifNotDisclaimed(context);
        } else if (context.conditionalValidator.isValidDisclaimer(disclaimer.toUpperCase())) {
            ifDisclaimed(context);
        } else {
            context.errors.add(createValidationError(productCode, "disclaimer", "Invalid disclaimer provided ", disclaimer, "A/F/' '"));
        }
    }

    private void ifDisclaimed(ValidationContext context) {
        String agencyCode = context.productDetails.getGovernmentAgencyCode();
        String processingCode = context.productDetails.getGovernmentAgencyProcessingCode();
        if (StringUtils.isNotBlank(agencyCode) && !processingCode.equalsIgnoreCase(agencyCode)) {
            context.errors.add(createValidationError(context.productCode, "governmentAgencyProcessingCode", "The line is disclaimed,Hence the value provided is not valid in this situation ", processingCode, agencyCode));
        }
        context.errors.add(createValidationError(context.productCode, "governmentAgencyProgramCode", "The line is disclaimed,Hence the program code is not valid in this scenario  ", context.programCode, agencyCode));
    }

    private void ifNotDisclaimed(ValidationContext context) {
        String processingCode = context.productDetails.getGovernmentAgencyProcessingCode();
        String intendedUseCode = context.productDetails.getIntendedUseCode();
        if (StringUtils.isNotBlank(processingCode) && !context.conditionalValidator.isValidProcessingCode(processingCode)) {
            context.errors.add(createValidationError(context.productCode, "governmentAgencyProcessingCode", "Provided governmentAgencyProcessingCode is not valid for the program code " + context.programCode, processingCode));
        }
        if (StringUtils.isNotBlank(intendedUseCode) && !context.conditionalValidator.isValidIntendedUseCode(intendedUseCode)) {
            context.errors.add(createValidationError(context.productCode, "intendedUseCode", "Provided intendedUseCode is not valid for the program code " + context.programCode, intendedUseCode));
        }
    }

    //productIdentifierValidation----------------------------------------------------------------
    public void validateProductIdentifier(ValidationContext context) {
        String itemType = context.productDetails.getItemType();
        String productCodeNumber = context.productCode;
        if (StringUtils.isNotBlank(itemType) && !context.conditionalValidator.isValidItemType(itemType)) {
            context.errors.add(createValidationError(productCodeNumber, "itemType", "Invalid itemType provided ", itemType, "P"));
        }
        if (StringUtils.isNotBlank(productCodeNumber) && !context.conditionalValidator.isValidProductCodeStructure(productCodeNumber)) {
            context.errors.add(createValidationError(productCodeNumber, "productCodeNumber", "Invalid product code number provided for the commodity " + context.programCode, productCodeNumber));
        }
    }

    //productOriginValidation----------------------------------------------------------------
    public void validateProductOrigin(ValidationContext context) {
        List<ProductOrigin> productOrigins = context.productDetails.getProductOrigin();
        if (productOrigins == null)
            return;
        if (productOrigins.isEmpty()) {
            context.errors.add(createValidationError(context.productCode(), "productOrigin", "At least one product origin should be provided for the commodity " + context.programCode, null, null));
        }
        Set<String> mandatorySourceCodeTypes = new HashSet<>(context.conditionalValidator.getMandatorySourceCode());
        Set<String> seenSourceCodeTypes = new HashSet<>();
        String productCode = context.productDetails.getProductCodeNumber();
        for (ProductOrigin origin : productOrigins) {
            String sourceTypeCode = origin.getSourceTypeCode();
            String countryCode = origin.getCountryCode();
            validateProductOriginFields(sourceTypeCode, countryCode, context);
            seenSourceCodeTypes.add(sourceTypeCode);
        }
        mandatorySourceCodeTypes.retainAll(seenSourceCodeTypes);
        if (mandatorySourceCodeTypes.isEmpty()) {
            context.errors.add(createValidationError(productCode, "productOrigin", "Missing mandatory Source code type: ", null, "Expected any of " + context.conditionalValidator.getMandatorySourceCode()));
        }
    }

    private void validateProductOriginFields(String sourceTypeCode, String countryCode, ValidationContext context) {
        if (StringUtils.isNotBlank(sourceTypeCode) && !context.conditionalValidator.isValidSourceCodeType(sourceTypeCode)) {
            context.errors.add(createValidationError(context.productCode, "sourceTypeCode", "Invalid source type code provided ", sourceTypeCode));
        }
        if (StringUtils.isNotBlank(countryCode) && !context.conditionalValidator.isValidCountryCode(countryCode)) {
            context.errors.add(createValidationError(context.productCode, "countryCode", "Invalid country code provided ", countryCode));
        }
    }
    //productTradeNameValidation----------------------------------------------------------------

    public void validateProductTradeNames(ValidationContext context) {
        TradeOrBrandNameInfo tradeOrBrandNameInfo = context.productDetails.getTradeOrBrandNameInfo();
        String processingCode = context.productDetails.getGovernmentAgencyProcessingCode();
        String pgSegment = "PG07";
        if (context.conditionalValidator.isTBNRequired(processingCode)) {
            if (tradeOrBrandNameInfo == null) {
                context.errors.add(createValidationError(context.productDetails.getProductCodeNumber(), "tradeOrBrandNameInfo", "Trade or Brand Name Information should be provided for the commodity " + context.programCode + " and Processing code " + processingCode, null, null));
            }
        } else {
            if (tradeOrBrandNameInfo != null) {
                List<AdditionalInformations> additionalInformations = tradeOrBrandNameInfo.getAdditionalInformations();
                validateAdditionalInformations(pgSegment, "tradeOrBrandNameInfos", additionalInformations, context);
            }
        }
    }
    //AdditionalInformationValidation-----------------------------------------------------------------------

    public void validateAdditionalInformations(String pgSegment,
                                               String objectName,
                                               List<AdditionalInformations> additionalInformations,
                                               ValidationContext context) {
        if (additionalInformations == null || additionalInformations.isEmpty()) {
            return;
        }
        for (AdditionalInformations additionalInfo : additionalInformations) {
            String code = additionalInfo.getAdditionalInformationQualifierCode();
            if (StringUtils.isNotBlank(code) && !context.conditionalValidator.isValidAdditionalInfoQualifierCode(pgSegment, code)) {
                context.errors.add(createValidationError(context.productCode, "additionalInformationQualifierCode",
                        "Invalid additional information code provided for " + objectName,
                        code, context.conditionalValidator.getAdditionalInfoQualifierCode(pgSegment).toString()));
            }
        }
    }

    //partyDetailsValidation------------------------------------------------------------------------------------------------
    public Set<String> validatePartyDetails(ValidationContext context) {
        List<EntityDetails> partyDetails = context.productDetails.getPartyDetails();
        String productCode = context.productDetails.getProductCodeNumber();
        if (partyDetails == null || partyDetails.isEmpty())
            return Collections.emptySet();
        Set<String> mandatoryPartyDetails = new HashSet<>(context.conditionalValidator.getMandatoryPartyTypes());
        Set<String> seenPartyDetails = new HashSet<>();
        for (EntityDetails entityDetails : partyDetails) {
            EntityData entityData = entityDetails.getEntityData();
            EntityAddress entityAddress = entityDetails.getEntityAddress();
            List<PointOfContact> pointOfContacts = entityDetails.getPointOfContacts();

            String party = validateEntityDataFields(entityData, context);
            validateEntityAddressFields(entityAddress, party, context);
            validatePointOfContacts(pointOfContacts, party, context);
            if (!seenPartyDetails.add(party) && !context.conditionalValidator.isPartyTypeRepeatable(party))
                context.errors.add(createValidationError(productCode, "partyType", "Duplicate party Type found for the commodity " + context.programCode, party));
        }
        mandatoryPartyDetails.removeAll(seenPartyDetails);
        if (!mandatoryPartyDetails.isEmpty())
            context.errors.add(createValidationError(productCode, "partyType", "Missing mandatory party type: ", seenPartyDetails.toString(), "Must provide " + mandatoryPartyDetails));
        return seenPartyDetails;
    }

    public String validateEntityDataFields(EntityData entityData,
                                           ValidationContext context) {
        String pgSegment = "PG19";
        String partyType = entityData.getPartyType();
        String identificationCode = entityData.getPartyIdentifierType();
        String entityNumber = entityData.getPartyIdentifierNumber();
        List<AdditionalInformations> additionalInformations = entityData.getAdditionalInformations();

        if (StringUtils.isNotBlank(partyType) && !context.conditionalValidator.isValidPartyType(partyType)) {
            context.errors.add(createValidationError(context.productCode, "partyType", "Invalid party type provided for the commodity " + context.programCode, partyType));
        }
        if (StringUtils.isBlank(identificationCode) && StringUtils.isNotBlank(entityNumber)) {
            context.errors.add(createValidationError(context.productCode, "partyIdentifierType", "Party identifier type should be provided for the commodity " + context.programCode, null, null));
        }
        if (StringUtils.isBlank(entityNumber) && StringUtils.isNotBlank(identificationCode)) {
            context.errors.add(createValidationError(context.productCode, "partyIdentifierNumber", "Party identifier number should be provided for the commodity " + context.programCode, null, null));
        }
        if (StringUtils.isNotBlank(identificationCode) && StringUtils.isNotBlank(entityNumber)) {
            if (!context.conditionalValidator.isValidPartyIdentifierType(identificationCode)) {
                context.errors.add(createValidationError(context.productCode, "partyIdentifierType", "Invalid party identifier type provided for the commodity " + context.programCode, identificationCode));
            }
            if (!context.conditionalValidator.isValidPartyIdentifierNumberSyntax(identificationCode, entityNumber)) {
                context.errors.add(createValidationError(context.productCode, "partyIdentifierNumber", "Invalid party identifier number syntax provided for the commodity " + context.programCode, entityNumber, getPartyIdentifierNumberErrorMessage(identificationCode)));
            }
        }
        validateAdditionalInformations(pgSegment, "entityData", additionalInformations, context);
        return partyType;
    }

    public void validateEntityAddressFields(EntityAddress entityAddress, String partyType, ValidationContext context) {
        String pgSegment = "PG20";
        String countryCode = entityAddress.getCountry();
        String postalCode = entityAddress.getPostalCode();
        String stateOrProvinceCode = entityAddress.getStateOrProvince();

        List<AdditionalInformations> additionalInformations = entityAddress.getAdditionalInformations();
        if (StringUtils.isNotBlank(countryCode)) {
            if (!context.conditionalValidator.isValidCountryCode(countryCode)) {
                context.errors.add(createValidationError(context.productCode, "country", "Invalid country code provided for the party " + partyType, countryCode));
            } else {
                if (context.conditionalValidator.isStateAndPostalCodeRequired(countryCode) && (StringUtils.isBlank(stateOrProvinceCode) || StringUtils.isBlank(postalCode))) {
                    context.errors.add(createValidationError(context.productCode, "stateOrProvince & postalCode", "State or Province and Postal Code should be provided for entity address for the party " + partyType, null, null));
                }
                if (StringUtils.isNotBlank(stateOrProvinceCode) && !context.conditionalValidator.isValidStateCode(countryCode, stateOrProvinceCode)) {
                    context.errors.add(createValidationError(context.productCode, "stateOrProvince", "Invalid state or province code provided for the party " + partyType, stateOrProvinceCode));
                }
            }
        }
        validateAdditionalInformations(pgSegment, "entityAddress", additionalInformations, context);
    }

    public void validatePointOfContacts(List<PointOfContact> pointOfContacts, String partyType, ValidationContext context) {
        if (pointOfContacts == null)
            return;
        String pgSegment = "PG21";
        Set<String> mandatoryIndividualQualifiers = new HashSet<>(context.conditionalValidator.getRequiredIndividualQualifier()); // this validation may move to the party details List level , Not in the individual party object
        Set<String> seenIndividualQualifiers = new HashSet<>();
        if (pointOfContacts.isEmpty()) {
            context.errors.add(createValidationError(context.productCode, "pointOfContacts", "pointOfContacts must contain mandatory individual Qualifiers for the Party " + partyType, 0, mandatoryIndividualQualifiers.toString()));
        } else {
            for (PointOfContact pointOfContact : pointOfContacts) {
                String individualQualifier = pointOfContact.getIndividualQualifier();
                List<AdditionalInformations> additionalInformations = pointOfContact.getAdditionalInformations();
                if (StringUtils.isNotBlank(individualQualifier) && !context.conditionalValidator.isValidIndividualQualifierCode(individualQualifier)) {
                    context.errors.add(createValidationError(context.productCode, "individualQualifier", "Invalid individual qualifier code provided for the Party " + partyType, individualQualifier));
                }
                seenIndividualQualifiers.add(individualQualifier);
                validateAdditionalInformations(pgSegment, "pointOfContacts", additionalInformations, context);
            }
            mandatoryIndividualQualifiers.removeAll(seenIndividualQualifiers);
            if (!mandatoryIndividualQualifiers.isEmpty()) {
                context.errors.add(createValidationError(context.productCode, "individualQualifier", "Missing mandatory individual qualifier for the party " + partyType,
                        seenIndividualQualifiers, "Must provide " + mandatoryIndividualQualifiers));
            }
        }
    }

    //ProductConditionValidation---------------------------------------------------------
    public void validateProductCondition(ValidationContext context) {
        List<ProductCondition> productConditions = context.productDetails.getProductCondition();
        if (productConditions == null || productConditions.isEmpty()) {
            return;
        }
        validateProductConditionInternal(productConditions.get(0), context, true);
        productConditions.stream()
                .skip(1)
                .forEach(condition -> validateProductConditionInternal(condition, context, false));
    }

    private void validateProductConditionInternal(ProductCondition condition, ValidationContext context, boolean isFirst) {
        validateField(condition.getTemperatureQualifier(), "temperatureQualifier",
                context.conditionalValidator::isValidTemperatureQualifierCode,
                "Invalid temperature qualifier code provided for product conditions related to the commodity %s",
                context, null);
        if (isFirst) {
            validateField(condition.getLotNumberQualifier(), "lotNumberQualifier",
                    context.conditionalValidator::isValidLotNumberQualifier,
                    "Invalid lot number qualifier code provided for product conditions related to the commodity %s",
                    context, context.conditionalValidator.getValidLotNumberQualifier());

            if (StringUtils.isBlank(condition.getPgaLineValue())) {
                context.errors.add(createValidationError(context.productCode, "pgaLineValue",
                        String.format("PGA Line Value is required for the first product condition related to the commodity %s", context.programCode),
                        null));
            }
        } else {
            checkUnexpectedField(condition.getLotNumberQualifier(), "lotNumberQualifier", context);
            checkUnexpectedField(condition.getPgaLineValue(), "pgaLineValue", context);
        }
    }

    private void validateField(String value, String fieldName, Predicate<String> validator, String errorMessage,
                               ValidationContext context, String additionalInfo) {
        if (StringUtils.isNotBlank(value) && !validator.test(value)) {
            context.errors.add(createValidationError(context.productCode, fieldName,
                    String.format(errorMessage, context.programCode), value, additionalInfo));
        }
    }

    private void checkUnexpectedField(String value, String fieldName, ValidationContext context) {
        if (StringUtils.isNotBlank(value)) {
            context.errors.add(createValidationError(context.productCode, fieldName,
                    String.format("%s should only be present in the first product condition for commodity %s", fieldName, context.programCode),
                    value));
        }
    }

    //productPackaging----------------------------------------------------------------
    public void validateProductPackaging(ValidationContext context) {
        List<List<ProductPackaging>> productPackagingDetails = context.productDetails.getProductPackaging();
        if (productPackagingDetails == null || productPackagingDetails.isEmpty()) {
            return;
        }
        for (List<ProductPackaging> productPackaging : productPackagingDetails) {
            if (productPackaging.size() > 6) {
                context.errors.add(createValidationError(context.productCode, "productPackaging",
                        String.format("No more than 6 packaging details are allowed for commodity %s", context.programCode),
                        productPackaging.size(), null));
                continue;
            }
            if (productPackaging.isEmpty()) {
                return;
            }
            validateProductPackagingList(productPackaging, context);
        }
    }

    private void validateProductPackagingList(List<ProductPackaging> packagingList, ValidationContext context) {
        List<String> validPackagingQualifierCode = context.conditionalValidator.getValidPackagingQualifierCode();
        String expectedQualifier = validPackagingQualifierCode.get(0);
        Set<String> seenPackagingQualifierCode = new HashSet<>();

        for (int i = 0; i < packagingList.size(); i++) {
            ProductPackaging packaging = packagingList.get(i);
            String packagingQualifier = packaging.getPackagingQualifier();
            String quantity = packaging.getQuantity();
            String uom = packaging.getUom();

            context.errors.addAll(checkInitialViolations(packaging));
            if (StringUtils.isNotBlank(packagingQualifier) && StringUtils.isNotBlank(quantity) && StringUtils.isNotBlank(uom)) {
                validatePackagingQualifier(packagingQualifier, validPackagingQualifierCode, expectedQualifier, context);
                validateUOM(uom, i == packagingList.size() - 1, context);

                if (packagingQualifier.equals(expectedQualifier) && expectedQualifier.compareTo(validPackagingQualifierCode.get(validPackagingQualifierCode.size() - 1)) < 0) {
                    expectedQualifier = validPackagingQualifierCode.get(validPackagingQualifierCode.indexOf(expectedQualifier) + 1);
                }
            }
            if (!seenPackagingQualifierCode.add(packagingQualifier)) {
                context.errors.add(createValidationError(context.productCode, "packagingQualifier",
                        String.format("Duplicate packaging qualifier %s for commodity %s", packagingQualifier, context.programCode),
                        packagingQualifier, "Packaging qualifiers must be unique"));
            }
        }
    }

    private void validatePackagingQualifier(String packagingQualifier, List<String> validPackagingQualifierCode, String expectedQualifier, ValidationContext context) {
        if (!validPackagingQualifierCode.contains(packagingQualifier)) {
            context.errors.add(createValidationError(context.productCode, "packagingQualifier",
                    String.format("Invalid packaging qualifier %s for commodity %s", packagingQualifier, context.programCode),
                    packagingQualifier, "Allowed values are -> " + validPackagingQualifierCode + " in order"));
        } else if (!packagingQualifier.equals(expectedQualifier)) {
            context.errors.add(createValidationError(context.productCode, "packagingQualifier",
                    String.format("Packaging qualifier %s is out of order for commodity %s", packagingQualifier, context.programCode),
                    packagingQualifier, "Packaging qualifiers must be in order: " + validPackagingQualifierCode));
        }
    }

    private void validateUOM(String uom, boolean isLastObject, ValidationContext context) {
        boolean isValid;
        String errorMessage;

        if (isLastObject) {
            isValid = context.conditionalValidator.isValidBaseUom(uom);
            errorMessage = "Last UOM must be a valid base UOM";
        } else {
            isValid = context.conditionalValidator.isValidUOMCode(uom);
            errorMessage = "UOM must be a valid UOM code";
        }
        if (!isValid) {
            context.errors.add(createValidationError(context.productCode, "uom",
                    String.format("Invalid UOM %s for commodity %s", uom, context.programCode),
                    uom, errorMessage));
        }
    }

    //AffirmationOfComplianceValidation----------------------------------------------------------------
    public Set<String> validateAffirmationOfCompliance(ValidationContext context) {

        String processingCode = context.productDetails().getGovernmentAgencyProcessingCode();
        String intendedUseCode = context.productDetails().getIntendedUseCode();
        String productCode = context.productCode();
        Set<String> seenAocCodes = new HashSet<>();

        List<AffirmationOfCompliance> affirmationOfCompliances = context.productDetails().getAffirmationOfCompliance();
        AffirmationOfComplianceContext complianceContext = new AffirmationOfComplianceContext(intendedUseCode, processingCode, context.conditionalValidator);

        if (affirmationOfCompliances == null || affirmationOfCompliances.isEmpty()) {
            handleEmptyAffirmations(complianceContext, productCode, context.errors());
            return Collections.emptySet();
        }
        for (AffirmationOfCompliance affirmationOfCompliance : affirmationOfCompliances) {
            String affirmationOfComplianceCode = affirmationOfCompliance.getAffirmationComplianceCode();
            if (StringUtils.isNotBlank(affirmationOfComplianceCode)) {
                handleSingleAffirmation(affirmationOfCompliance, complianceContext, context);
                if (!seenAocCodes.add(affirmationOfComplianceCode.toUpperCase()))
                    context.errors().add(createValidationError(productCode, "affirmationOfComplianceCode", "Affirmation of Compliance Code must be unique", affirmationOfComplianceCode));
            }
        }
        handleLastChecksForAffirmation(complianceContext, seenAocCodes, context);
        return seenAocCodes;
    }

    private void handleSingleAffirmation(AffirmationOfCompliance affirmationOfCompliance, AffirmationOfComplianceContext context, ValidationContext validationContext) {
        String affirmationOfComplianceCode = affirmationOfCompliance.getAffirmationComplianceCode();
        String affirmationOfComplianceQualifier = affirmationOfCompliance.getAffirmationComplianceQualifier();

        if (!context.isValidCode(affirmationOfComplianceCode)) {
            if (context.isMandatory) {
                validationContext.errors().add(createValidationError(validationContext.productCode(), "affirmationOfComplianceCode", String.format("Provided Affirmation of Compliance Code is not valid or not required for provided intendedUseCode %s and processingCode %s", context.intendedUseCode, context.processingCode), affirmationOfComplianceCode));
                return;
            } else {
                validationContext.errors().add(createValidationError(validationContext.productCode(), "affirmationOfComplianceCode", "Provided Affirmation of Code is not valid for the scenario ", affirmationOfComplianceCode));
            }
        }

        String syntax = validationContext.conditionalValidator.getAOCQSynatx(affirmationOfComplianceCode);
        if (syntax != null) {
            if (StringUtils.isNotBlank(affirmationOfComplianceQualifier) && !affirmationOfComplianceQualifier.matches(syntax)) {
                validationContext.errors().add(createValidationError(validationContext.productCode(), "affirmationOfComplianceQualifier",
                        DataViolationMessages.getAOCQSyntaxErrorMessage(validationContext.programCode, affirmationOfComplianceCode), affirmationOfComplianceQualifier, "Must follow -> " + syntax + "pattern"));
            } else if (StringUtils.isBlank(affirmationOfComplianceQualifier)) {
                validationContext.errors().add(createValidationError(validationContext.productCode(), "affirmationOfComplianceQualifier",
                        String.format("Affirmation of Compliance Qualifier is required for the affirmationOfComplianceCode %s", affirmationOfComplianceCode), null, null));
            }
        }
    }

    private void handleEmptyAffirmations(AffirmationOfComplianceContext context, String productCode, List<ValidationError> errors) {
        if (context.isMandatory()) {
            errors.add(createValidationError(productCode, "affirmationOfCompliance",
                    String.format("Affirmation of compliance required for provided processing code %s and intendedUseCode %s",
                            context.processingCode, context.intendedUseCode),
                    null, null));
        }
    }

    private void handleLastChecksForAffirmation(AffirmationOfComplianceContext context, Set<String> seenAocCodes, ValidationContext validationContext) {
        context.mandatoryAocCodes.removeAll(seenAocCodes);
        if (!context.mandatoryAocCodes.isEmpty()) {
            validationContext.errors().add(createValidationError(validationContext.productCode(), "affirmationOfCompliance",
                    String.format("Affirmation of compliance required all the mandatory codes for provided processing code %s and intendedUseCode %s",
                            context.processingCode, context.intendedUseCode),
                    seenAocCodes.toString(), context.mandatoryAocCodes.toString()));
        }
        if (!context.anyOfAocCodes.isEmpty()) {
            Set<String> anyAocCodesClone = new HashSet<>(context.anyOfAocCodes);
            anyAocCodesClone.retainAll(seenAocCodes);
            if (anyAocCodesClone.isEmpty()) {
                validationContext.errors().add(createValidationError(validationContext.programCode(), "affirmationOfCompliance",
                        String.format("Affirmation of compliance required at least one of the mandatory codes for provided processing code %s and intendedUseCode %s",
                                context.processingCode, context.intendedUseCode),
                        seenAocCodes, context.anyOfAocCodes.toString()));
            }
        }
    }

    @Data
    private static class AffirmationOfComplianceContext {
        private Set<String> mandatoryAocCodes = new HashSet<>();
        private Set<String> optionalAocCodes = new HashSet<>();
        private Set<String> anyOfAocCodes = new HashSet<>();
        private Set<String> allValidAocForScenario = new HashSet<>();
        private String processingCode;
        private String intendedUseCode;
        private boolean isMandatory;

        AffirmationOfComplianceContext(String intendedUseCode, String processingCode, ConditionalValidator conditionalValidator) {
            Map<String, Set<String>> validAocCodesForScenario = conditionalValidator.getScenarioBasedAocCode(processingCode, intendedUseCode);

            this.mandatoryAocCodes.addAll(validAocCodesForScenario.getOrDefault("mandatory", new HashSet<>()));
            this.optionalAocCodes.addAll(validAocCodesForScenario.getOrDefault("optional", new HashSet<>()));
            this.anyOfAocCodes.addAll(validAocCodesForScenario.getOrDefault("anyOf", new HashSet<>()));

            this.allValidAocForScenario.addAll(mandatoryAocCodes);
            this.allValidAocForScenario.addAll(optionalAocCodes);
            this.allValidAocForScenario.addAll(anyOfAocCodes);

            this.isMandatory = !this.mandatoryAocCodes.isEmpty() || !this.anyOfAocCodes.isEmpty();
            this.processingCode = processingCode;
            this.intendedUseCode = intendedUseCode;
        }

        private boolean isValidCode(String code) {
            return allValidAocForScenario.contains(code.toUpperCase());
        }
    }

    //Anticipated Arrival Information Validation
    public void validateAnticipatedArrivalLocation(ValidationContext context) {
        if(context.declaration != null ){
            String entryType = context.declaration.getEntryType();
            if (StringUtils.isNotBlank(entryType) && declarationRules.isValidEntryType(entryType)) {
                List<AnticipatedArrivalInformations> arrivalInformations = context.productDetails.getAnticipatedArrivalInformations();
                if (!isNullOrEmptyCollection(arrivalInformations)) {
                    boolean isComingFromForeignTradeZone = context.conditionalValidator.isForeignTradeZoneEntry(entryType);
                    validateFTZEntry(context, isComingFromForeignTradeZone);
                }
            }
        }

    }

    private void validateFTZEntry(ValidationContext context, boolean isFtzEntry) {
        List<AnticipatedArrivalInformations> arrivalInformations = context.productDetails.getAnticipatedArrivalInformations();
        List<String> mandatoryArrivalInformations = new ArrayList<>(context.conditionalValidator.getMandatoryAnticipatedArrivalInformation(isFtzEntry));
        String ftzArrivalInformation = context.conditionalValidator.getFtzArrivalInformation();

        for (AnticipatedArrivalInformations arrival : arrivalInformations) {
            context.errors.addAll(checkInitialViolations(arrival));
            validateSingleArrivalInformation(context, arrival, mandatoryArrivalInformations, ftzArrivalInformation);
        }

        checkMissingMandatoryInformation(context, mandatoryArrivalInformations);
    }

    private void validateSingleArrivalInformation(ValidationContext context, AnticipatedArrivalInformations arrival,
                                                  List<String> mandatoryArrivalInformations, String ftzArrivalInformation) {
        String anticipatedArrivalInformation = arrival.getAnticipatedArrivalInformation().toUpperCase();

        if (!mandatoryArrivalInformations.contains(anticipatedArrivalInformation)) {
            addInvalidArrivalInformationError(context, anticipatedArrivalInformation, mandatoryArrivalInformations);
            return;
        }

        if (isFtzArrivalInformation(anticipatedArrivalInformation, ftzArrivalInformation)) {
            validateFtzArrivalInformation(context, arrival);
        } else {
            validateNonFtzArrivalInformation(context, arrival);
        }

        mandatoryArrivalInformations.remove(anticipatedArrivalInformation);
    }

    private boolean isFtzArrivalInformation(String anticipatedArrivalInformation, String ftzArrivalInformation) {
        return ftzArrivalInformation != null && ftzArrivalInformation.equalsIgnoreCase(anticipatedArrivalInformation);
    }

    private void validateFtzArrivalInformation(ValidationContext context, AnticipatedArrivalInformations arrival) {
        if (StringUtils.isBlank(arrival.getInspectionOrArrivalLocation())) {
            addError(context, "inspectionOrArrivalLocation", "Inspection or Arrival Location is required for FTZ Anticipated Arrival Information");
        }
        if (StringUtils.isBlank(arrival.getInspectionOrArrivalLocationCode())) {
            addError(context, "inspectionOrArrivalLocationCode", "Inspection or Arrival Location Code is required for FTZ Anticipated Arrival Information");
        } else {
            if (!context.conditionalValidator.isValidInspectionOrArrivalLocationCodeForFtz(arrival.getInspectionOrArrivalLocationCode())) {
                addError(context, "inspectionOrArrivalLocationCode", "Invalid Inspection or Arrival Location Code for FTZ Anticipated Arrival Information");
            }
        }
    }

    private void validateNonFtzArrivalInformation(ValidationContext context, AnticipatedArrivalInformations arrival) {
        if (StringUtils.isBlank(arrival.getAnticipatedArrivalDate())) {
            addError(context, "anticipatedArrivalDate", "Anticipated Arrival Date is required for Anticipated Arrival Information");
        }
        if (StringUtils.isBlank(arrival.getAnticipatedArrivalTime())) {
            addError(context, "anticipatedArrivalTime", "Anticipated Arrival Time is required for Anticipated Arrival Information");
        }
    }

    private void addInvalidArrivalInformationError(ValidationContext context, String anticipatedArrivalInformation, List<String> mandatoryArrivalInformations) {
        context.errors.add(createValidationError(context.productCode, "anticipatedArrivalInformation",
                "Invalid anticipatedArrivalInformation provided ", anticipatedArrivalInformation, mandatoryArrivalInformations.toString()));
    }

    private void checkMissingMandatoryInformation(ValidationContext context, List<String> mandatoryArrivalInformations) {
        if (!mandatoryArrivalInformations.isEmpty()) {
            context.errors.add(createValidationError(context.productCode, "anticipatedArrivalInformation",
                    "Missing mandatory Anticipated Arrival Information", null, mandatoryArrivalInformations.toString()));
        }
    }

    private void addError(ValidationContext context, String field, String message) {
        context.errors.add(createValidationError(context.productCode, field, message, null, null));
    }

    //License plate issuer validations -------------------------------------------------------
    public void validateLicensePlateIssuer(ValidationContext context) {
        LicensePlateIssuer licensePlateIssuer = context.productDetails.getLicensePlateIssuer();
        if (licensePlateIssuer != null) {
            //            String issuerOfLPCO = licensePlateIssuer.getIssuerOfLPCO(); // for future use
            String governmentGeographicCodeQualifier = licensePlateIssuer.getGovernmentGeographicCodeQualifier();
            String locationOfIssuerOfTheLPCO = licensePlateIssuer.getLocationOfIssuerOfTheLPCO();
            String issuingAgencyLocation = licensePlateIssuer.getIssuingAgencyLocation();
            if (StringUtils.isNotBlank(governmentGeographicCodeQualifier)) {
                if (!context.conditionalValidator.isValidGovernmentGeographicCodeQualifier(governmentGeographicCodeQualifier))
                    context.errors.add(createValidationError(context.productCode, "governmentGeographicCodeQualifier", "Invalid Government Geographic Code Qualifier", governmentGeographicCodeQualifier, null));
                if (StringUtils.isNotBlank(locationOfIssuerOfTheLPCO) &&
                        !context.conditionalValidator.isValidLocationCode(locationOfIssuerOfTheLPCO, governmentGeographicCodeQualifier) &&
                        StringUtils.isBlank(issuingAgencyLocation)) {
                    context.errors.add(createValidationError(context.productCode, "locationOfIssuerOfTheLPCO", "Invalid Location Code provided with the governmentGeographicCodeQualifier " + governmentGeographicCodeQualifier, locationOfIssuerOfTheLPCO, null));
                }
            }
            LicensePlateNumber licensePlateNumber = context.productDetails.getLicensePlateNumber();
            if (licensePlateNumber == null) {
                context.errors.add(createValidationError(context.productCode, "licensePlateNumber", "License Plate Number is required", null, null));
            }
        }
    }

    //license plate number validation ------------------------------------------------
    public void validateLicensePlateNumber(ValidationContext context) {
        LicensePlateNumber licensePlateNumber = context.productDetails.getLicensePlateNumber();
        if (licensePlateNumber != null) {
            String lpcoOrCodeType = licensePlateNumber.getLpcoOrCodeType();
            String lpcoOrPncNumber = licensePlateNumber.getLpcoOrPncNumber();
            if(StringUtils.isNotBlank(lpcoOrCodeType) && context.conditionalValidator.isPrivatelyOwnedVehicle(lpcoOrCodeType)){
                LicensePlateIssuer licensePlateIssuer = context.productDetails.getLicensePlateIssuer();
                if(licensePlateIssuer == null){
                    context.errors.add(createValidationError(context.productCode, "licensePlateIssuer", "License Plate Issuer information mandatory for privately owned vehicles", null, null));
                }
                if(StringUtils.isBlank(lpcoOrPncNumber)){
                    context.errors.add(createValidationError(context.productCode, "lpcoOrPncNumber", "Privately Owned Vehicle LPCO or PNC Number is required", null, null));
                }
            }
        }
    }


}
