package com.customs.network.fdapn.validations.commodityvalidationimpl;

import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.validations.CommodityValidator;
import com.customs.network.fdapn.validations.constants.ConditionalValidator;
import com.customs.network.fdapn.validations.constants.DRUCommodityConstants;
import com.customs.network.fdapn.validations.constants.ProductCodeValidator;
import com.customs.network.fdapn.validations.objects.commodity.*;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.customs.network.fdapn.validations.utils.ErrorUtils.createValidationError;

@Component
public class DRUCommodityValidator extends CommonValidations implements CommodityValidator, SegmentValidator {
    private static final String PROGRAMME_CODE = "DRU";
    private final ConditionalValidator conditionalValidator;
    private final ProductCodeValidator productCodeValidator;

    public DRUCommodityValidator(DRUCommodityConstants druCommodityConstants) {
        this.conditionalValidator = druCommodityConstants;
        this.productCodeValidator = druCommodityConstants;
    }

    @Override
    public List<ValidationError> validate(ProductDetails productDetails) {
        List<ValidationError> errors = new ArrayList<>();
        String productCode = productDetails.getProductCodeNumber();
        ValidationContext context = new ValidationContext(productCode, errors, conditionalValidator, PROGRAMME_CODE, productDetails,null);
        validateRequiredFields(context);
        validatePGAIdentifier(context);
        validateProductIdentifier(context);
        validateProductConstituentElement(context);
        validateProductOrigin(context);
        validateProductTradeNames(context);
        validatePartyDetails(context);
        validateAffirmationOfCompliance(context);
        validateProductOrigin(context);
        validateProductCondition(context);
        validateProductPackaging(context);
        return errors;
    }


    //PGA identifier validation ----------------------------------------------------------------
    @Override
    public void validatePGAIdentifier(ValidationContext context) {
        super.validatePGAIdentifier(context);
        String processingCode = context.productDetails().getGovernmentAgencyProcessingCode();
        String intendedUseCode = context.productDetails().getIntendedUseCode();
        if(StringUtils.isNotBlank(processingCode) && StringUtils.isNotBlank(intendedUseCode) && !isIntendedUseCodeValidForScenario(processingCode, intendedUseCode)) {
                context.errors().add(createValidationError(context.productCode(), "intendedUseCode", "Provided intendedUseCode is not valid for the governmentAgencyProcessingCode " + intendedUseCode, processingCode));
            }
    }

    //product identifier validation ----------------------------------------------------------------
    @Override
    public void validateProductIdentifier(ValidationContext context) {
        String productCode = context.productCode();
        if (StringUtils.isNotBlank(productCode) && context.conditionalValidator().isValidProductCodeStructure(productCode)) {
            validateProductCodeStructure(context);
        } else {
            context.errors().add(createValidationError(productCode, "productCodeNumber",
                    "Invalid product code structure provided for the commodity " + context.programCode(),
                    productCode));
        }
    }

    private void validateProductCodeStructure(ValidationContext context) {
        String productCode = context.productCode();
        String processingCode = context.productDetails().getGovernmentAgencyProcessingCode();
        String intendedUseCode = context.productDetails().getIntendedUseCode();
        if (isIntendedUseCodeValidForScenario(processingCode, intendedUseCode)) {
            String industryCode = productCode.substring(0, 2);
            String subClassCode = String.valueOf(productCode.charAt(3));
            String processIndicatorCode = String.valueOf(productCode.charAt(4));
            if (!productCodeValidator.isValidIndustryCode(processingCode, industryCode)) {
                context.errors().add(createValidationError(productCode, "productCodeNumber",
                        String.format("Industry code provided in the product code number is invalid for processing code %s ", processingCode), industryCode));
            } else {
                if (!productCodeValidator.isValidSubClassCode(processingCode, industryCode, subClassCode)) {
                    context.errors().add(createValidationError(productCode, "productCodeNumber",
                            String.format("Sub-class code provided in the product code number is invalid for processing code %s and industry code %s ", processingCode, industryCode), subClassCode));
                }
                if (!productCodeValidator.isValidProcessIndicatorCode(processingCode, intendedUseCode, processIndicatorCode)) {
                    context.errors().add(createValidationError(productCode, "productCodeNumber",
                            String.format("Process indicator code provided in the product code number is invalid for processing code %s, intended use code %s ", processingCode, intendedUseCode), processIndicatorCode));
                }
            }
        }
    }

    //product constituent element validation ----------------------------------------------------------------
    @Override
    public void validateProductConstituentElement(ValidationContext context) {
        String processingCode = context.productDetails().getGovernmentAgencyProcessingCode();
        List<ProductConstituentElement> constituentElements = context.productDetails().getProductConstituentElements();
        if (doNullCheckForConstituentElementList(context, constituentElements, processingCode)) {
            return;
        }
        for (ProductConstituentElement constituentElement : constituentElements) {
            validateSingleConstituentElement(constituentElement, context);
        }

    }

    private void validateSingleConstituentElement(ProductConstituentElement constituentElement, ValidationContext context) {
        String intendedUseCode = context.productDetails().getIntendedUseCode();
        String elementName = constituentElement.getConstituentElementName();
        String quantity = constituentElement.getConstituentElementQuantity();
        String unitOfMeasure = constituentElement.getConstituentElementUnitOfMeasure();
        String percent = constituentElement.getPercentOfConstituentElement();
        String qualifier = constituentElement.getConstituentActiveIngredientQualifier();

        doNullCheckForConstituentElementFields(context, elementName, percent, unitOfMeasure, quantity, intendedUseCode);
        validateQualifier(context, elementName, qualifier);
        validateQuantityFormat(context, elementName, quantity);
        validatePercentFormat(context, elementName, percent);
        validateUnitOfMeasure(context, elementName, unitOfMeasure);
    }

    // Helper methods for specific constituent element validations ----------------------------------------------------------------
    private void validateQualifier(ValidationContext context, String elementName, String qualifier) {
        if (StringUtils.isNotBlank(qualifier) && !context.conditionalValidator().isValidConstituentActiveIngredient(qualifier)) {
            context.errors().add(createValidationError(context.productCode(), "constituentActiveIngredientQualifier",
                    "Invalid constituent active ingredient qualifier provided for constituent element " + elementName, qualifier));
        }
    }

    private void validateQuantityFormat(ValidationContext context, String elementName, String quantity) {
        if (StringUtils.isNotBlank(quantity) && quantity.length() < 3) {
            context.errors().add(createValidationError(context.productCode(), "constituentElementQuantity",
                    "Constituent element quantity must have at least 3 characters for constituent element " + elementName, quantity,
                    "2 decimal places are implied. Example: for 4 quantity, value should be 400 in this format"));
        }
    }

    private void validatePercentFormat(ValidationContext context, String elementName, String percent) {
        if (StringUtils.isNotBlank(percent) && percent.length() < 5) {
            context.errors().add(createValidationError(context.productCode(), "percentOfConstituentElement",
                    "Constituent element percent must have at least 5 characters for constituent element " + elementName, percent,
                    "4 decimal places are implied. Example: for 40 percent, value should be 40000 in this format"));
        }
    }

    private void validateUnitOfMeasure(ValidationContext context, String elementName, String unitOfMeasure) {
        if (StringUtils.isNotBlank(unitOfMeasure) && !context.conditionalValidator().isValidUOMCode(unitOfMeasure)) {
            context.errors().add(createValidationError(context.productCode(), "constituentElementUnitOfMeasure",
                    "Invalid unit of measure provided for constituent element " + elementName, unitOfMeasure));
        }
    }

    private void doNullCheckForConstituentElementFields(ValidationContext context, String elementName, String percent, String unitOfMeasure, String quantity, String intendedUseCode) {
        if (context.conditionalValidator().isRequiredEveryConstituentElementFields(intendedUseCode)) {
            if (StringUtils.isBlank(unitOfMeasure) || StringUtils.isBlank(quantity) || StringUtils.isBlank(percent)) {
                context.errors().add(createValidationError(context.productCode(), "productConstituentElements",
                        "All of uom, quantity, and percent must be provided for constituent element " + elementName,
                        "One or more fields are missing or null"));
            }
        } else {
            if ((StringUtils.isBlank(unitOfMeasure) || StringUtils.isBlank(quantity)) && StringUtils.isBlank(percent)) {
                context.errors().add(createValidationError(context.productCode(), "productConstituentElements",
                        "Either uom and quantity or percent must be provided for constituent element " + elementName,
                        "One of uom/quantity pair or percent must be provided"));
            }
        }
    }

    private boolean doNullCheckForConstituentElementList(ValidationContext context, List<ProductConstituentElement> constituentElements, String processingCode) {
        boolean isNotValidList = false;
        if (constituentElements == null || constituentElements.isEmpty()) {
            if (context.conditionalValidator().isConstituentElementRequired(processingCode)) {
                context.errors().add(createValidationError(context.productCode(), "productConstituentElements",
                        "Constituent element must provide when you are using processing code " + processingCode, "[] or null"));
            }
            isNotValidList = true;
        }
        return isNotValidList;
    }

    //Party details validation ----------------------------------------------------------------
    @Override
    public Set<String> validatePartyDetails(ValidationContext context) {
        super.validatePartyDetails(context);
        List<EntityDetails> partyDetails = context.productDetails().getPartyDetails();
        if (partyDetails == null || partyDetails.isEmpty())
            return Collections.emptySet();
        String processingCode = context.productDetails().getGovernmentAgencyProcessingCode();
        for (EntityDetails entityDetails : partyDetails) {
            EntityData entityData = entityDetails.getEntityData();
            EntityAddress entityAddress = entityDetails.getEntityAddress();
            String partyType = entityData.getPartyType();
            String countryCode = entityAddress.getCountry();
            if (context.conditionalValidator().isValidCountryCode(countryCode) && !context.conditionalValidator().isValueExcluded(processingCode, partyType, countryCode)) {
                context.errors().add(createValidationError(context.productCode(), "country",
                        String.format("The provided country code %s is not valid for the party %s, When you use processing code %s", countryCode, partyType, processingCode), countryCode));
            }
        }
        return Collections.emptySet();
    }

    //Validate affirmation of compliance ----------------------------------------------------------------
    @Override
    public Set<String> validateAffirmationOfCompliance(ValidationContext context) {
        String intendedUseCode = context.productDetails().getIntendedUseCode();
        String processingCode = context.productDetails().getGovernmentAgencyProcessingCode();
        if (isIntendedUseCodeValidForScenario(processingCode, intendedUseCode)) {
            return super.validateAffirmationOfCompliance(context);
        }
        return Collections.emptySet();
    }

    //Validate product condition ----------------------------------------------------------------
    @Override
    public void validateProductCondition(ValidationContext context) {
        super.validateProductCondition(context);
        List<ProductCondition> productConditions = context.productDetails().getProductCondition();
        if (productConditions == null || productConditions.isEmpty()) {
            return;
        }
        String processingCode = context.productDetails().getGovernmentAgencyProcessingCode();
        for (ProductCondition productCondition : productConditions) {
            String degreeType = productCondition.getDegreeType();
            String locationOfTemperatureRecording = productCondition.getLocationOfTemperatureRecording();
            String temperatureIndicator = productCondition.getNegativeNumber();
            String lotNumber = productCondition.getLotNumber();
            if (StringUtils.isNotBlank(degreeType) && !context.conditionalValidator().isValidDegreeType(degreeType)) {
                context.errors().add(createValidationError(context.productCode(), "degreeType",
                        String.format("Invalid degree type %s provided for product condition", degreeType), degreeType));
            }
            if (StringUtils.isNotBlank(locationOfTemperatureRecording) && !context.conditionalValidator().isValidLocationOfTemperatureRecording(locationOfTemperatureRecording)) {
                context.errors().add(createValidationError(context.productCode(), "locationOfTemperatureRecording",
                        String.format("Invalid location of temperature recording %s provided for product condition", locationOfTemperatureRecording), locationOfTemperatureRecording));
            }
            if (StringUtils.isNotBlank(temperatureIndicator) && !context.conditionalValidator().isValidTemperatureIndicator(temperatureIndicator)) {
                context.errors().add(createValidationError(context.productCode(), "negativeNumber",
                        String.format("Invalid negative number %s provided for product condition", temperatureIndicator), temperatureIndicator));
            }
            if (StringUtils.isBlank(lotNumber) && !context.conditionalValidator().isLotNumberRequired(processingCode)) {
                context.errors().add(createValidationError(context.productCode(), "lotNumber",
                        "Lot number must provide when you are using processing code " + processingCode, "null or blank"));
            }

        }
    }
    //Validate product packaging ----------------------------------------------------------------


    //Common Helper methods ----------------------------------------------------------------
    private boolean isIntendedUseCodeValidForScenario(String processingCode, String intendedUseCode) {
        return !StringUtils.isNotBlank(intendedUseCode) ||
                !conditionalValidator.isValidProcessingCode(processingCode) ||
                conditionalValidator.isValidIntendedUseCode(processingCode, intendedUseCode);
    }

    @Override
    public ConditionalValidator getConditionalValidator(){
        return conditionalValidator;
    }
}
