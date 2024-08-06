package com.customs.network.fdapn.validations.commodityvalidationimpl;

import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.validations.CommodityValidator;
import com.customs.network.fdapn.validations.constants.ConditionalValidator;
import com.customs.network.fdapn.validations.constants.TOBCommodityConstants;
import com.customs.network.fdapn.validations.objects.commodity.ProductDetails;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.customs.network.fdapn.validations.utils.ErrorUtils.createValidationError;

@Component
public class TOBCommodityValidator extends CommonValidations implements CommodityValidator,SegmentValidator {
    private static final String PROGRAMME_CODE = "TOB";
    private final ConditionalValidator conditionalValidator;

    public TOBCommodityValidator(TOBCommodityConstants tobCommodityConstants) {
        this.conditionalValidator = tobCommodityConstants;
    }

    @Override
    public List<ValidationError> validate(ProductDetails productDetails) {
        List<ValidationError> errors = new ArrayList<>();
        String productCode = productDetails.getProductCodeNumber();
        ValidationContext context = new ValidationContext(productCode, errors, conditionalValidator, PROGRAMME_CODE, productDetails,null);
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

    //validate pga identifier ----------------------------------------------------------------
    @Override
    public void validatePGAIdentifier(ValidationContext context) {
        super.validatePGAIdentifier(context);
        String disclaimer = context.productDetails().getDisclaimer();
        String processingCode = context.productDetails().getGovernmentAgencyProcessingCode();
        String intendedUseCode = context.productDetails().getIntendedUseCode();
        if (StringUtils.isBlank(disclaimer) &&
                StringUtils.isNotBlank(processingCode) &&
                StringUtils.isNotBlank(intendedUseCode) &&
                !context.conditionalValidator().isValidIntendedUseCode(processingCode, intendedUseCode)) {
            context.errors().add(createValidationError(context.productCode(), "intendedUseCode",
                    "Provided intendedUseCode is Not valid when you are using processing code " + processingCode,
                    intendedUseCode, null));
        }
    }

    @Override
    public void validateProductConstituentElement(ValidationContext context) {

    }

    //party details additional validations ----------------------------------------------------------------
    @Override
    public Set<String> validatePartyDetails(ValidationContext context) {
        Set<String> seenPartyTypes = super.validatePartyDetails(context);
        String processingCode = context.productDetails().getGovernmentAgencyProcessingCode();
        Set<String> requiredConditionalPartyTypes = context.conditionalValidator().getConditionalPartyTypes(processingCode);
        if (!requiredConditionalPartyTypes.isEmpty()) {
            boolean hasConditionalPartyType = requiredConditionalPartyTypes.stream()
                    .anyMatch(seenPartyTypes::contains);
            if (!hasConditionalPartyType) {
                context.errors().add(createValidationError(context.productCode(), "partyDetails",
                        "At least one party type should be present in the product condition for processing code " + processingCode,
                        seenPartyTypes, requiredConditionalPartyTypes));
            }
        }
        return Collections.emptySet();
    }

    @Override
    public ConditionalValidator getConditionalValidator(){
        return conditionalValidator;
    }
}
