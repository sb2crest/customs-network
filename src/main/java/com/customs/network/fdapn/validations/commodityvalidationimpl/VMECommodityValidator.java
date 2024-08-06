package com.customs.network.fdapn.validations.commodityvalidationimpl;

import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.validations.CommodityValidator;
import com.customs.network.fdapn.validations.constants.ConditionalValidator;
import com.customs.network.fdapn.validations.constants.ProductCodeValidator;
import com.customs.network.fdapn.validations.constants.VMECommodityConstants;
import com.customs.network.fdapn.validations.objects.commodity.ProductConstituentElement;
import com.customs.network.fdapn.validations.objects.commodity.ProductDetails;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.customs.network.fdapn.validations.utils.ErrorUtils.createValidationError;

@Component
public class VMECommodityValidator extends CommonValidations implements CommodityValidator,SegmentValidator {
    private static final String PROGRAMME_CODE = "VME";
    private final ConditionalValidator conditionalValidator;
    private final ProductCodeValidator productCodeValidator;

    public VMECommodityValidator(VMECommodityConstants vmeCommodityConstants) {
        this.conditionalValidator = vmeCommodityConstants;
        this.productCodeValidator = vmeCommodityConstants;
    }

    @Override
    public List<ValidationError> validate(ProductDetails productDetails) {
        List<ValidationError> errors = new ArrayList<>();
        String productCode = productDetails.getProductCodeNumber();
        ValidationContext context = new ValidationContext(productCode, errors, conditionalValidator, PROGRAMME_CODE, productDetails,null);
        validatePGAIdentifier(context);
        validateProductIdentifier(context);
        validateProductConstituentElement(context);
        validateProductOrigin(context);
        validateProductTradeNames(context);
        validatePartyDetails(context);
        validateAffirmationOfCompliance(context);
        validateProductCondition(context);
        validateProductPackaging(context);
        return errors;
    }

    //additional validation for pga identifier ----------------------------------------------------------------
    @Override
    public void validatePGAIdentifier(ValidationContext context) {
        super.validatePGAIdentifier(context);
        String processingCode = context.productDetails().getGovernmentAgencyProcessingCode();
        String intendedUseCode = context.productDetails().getIntendedUseCode();
        if (StringUtils.isNotEmpty(processingCode) && context.conditionalValidator().isRequiredIntendedUseCode(processingCode) && StringUtils.isBlank(intendedUseCode)) {
            context.errors().add(createValidationError(context.productCode(), "intendedUseCode", "Intended Use Code is required when processing code " + processingCode, intendedUseCode, null));
        }
    }

    //additional validation for product identifier ----------------------------------------------------------------
    @Override
    public void validateProductIdentifier(ValidationContext context) {
        super.validateProductIdentifier(context);
        String productCode = context.productCode();
        String processingCode = context.productDetails().getGovernmentAgencyProcessingCode();
        if (StringUtils.isNotBlank(productCode) && StringUtils.isNotBlank(processingCode)) {
            validateProductCode(context, productCode, processingCode);
        }
    }

    private void validateProductCode(ValidationContext context, String productCode, String processingCode) {
        if (context.conditionalValidator().isValidProductCodeStructure(productCode)) {
            String industryCode = productCode.substring(0, 2);
            String subClassCode = processingCode.substring(4, 5);
            if (!productCodeValidator.isValidIndustryCode(processingCode, industryCode)) {
                context.errors().add(createValidationError(productCode, "productCodeNumber",
                        String.format("Industry code provided in the product code number is invalid for processing code %s ", processingCode), industryCode));
            }
            if (!productCodeValidator.isValidSubClassCode(processingCode, industryCode, subClassCode)) {
                context.errors().add(createValidationError(productCode, "productCodeNumber",
                        String.format("Sub-class code provided in the product code number is invalid for processing code %s and industry code %s ", processingCode, industryCode), subClassCode));
            }
        }
    }

    //product constituent element validation ----------------------------------------------------------------
    public void validateProductConstituentElement(ValidationContext context) {
        List<ProductConstituentElement> constituentElements = context.productDetails().getProductConstituentElements();
        if (constituentElements == null || constituentElements.isEmpty()) {
            return;
        }
        for (ProductConstituentElement constituentElement : constituentElements) {
            validateSingleConstituentElement(constituentElement, context);
        }
    }

    private void validateSingleConstituentElement(ProductConstituentElement constituentElement, ValidationContext context) {
        String elementName = constituentElement.getConstituentElementName();
        String quantity = constituentElement.getConstituentElementQuantity();
        String unitOfMeasure = constituentElement.getConstituentElementUnitOfMeasure();
        String percent = constituentElement.getPercentOfConstituentElement();
        String qualifier = constituentElement.getConstituentActiveIngredientQualifier();

        doNullCheckForConstituentElementFields(context, elementName, percent, unitOfMeasure, quantity);
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

    private void doNullCheckForConstituentElementFields(ValidationContext context, String elementName, String percent, String unitOfMeasure, String quantity) {
            if ((StringUtils.isBlank(unitOfMeasure) || StringUtils.isBlank(quantity)) && StringUtils.isBlank(percent)) {
                context.errors().add(createValidationError(context.productCode(), "productConstituentElements",
                        "Either uom and quantity or percent must be provided for constituent element " + elementName,
                        "One of uom/quantity pair or percent must be provided"));
            }
    }

    @Override
    public ConditionalValidator getConditionalValidator(){
        return conditionalValidator;
    }
}
