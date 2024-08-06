package com.customs.network.fdapn.validations.commodityvalidationimpl;

import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.validations.CommodityValidator;
import com.customs.network.fdapn.validations.constants.ConditionalValidator;
import com.customs.network.fdapn.validations.constants.RADCommodityConstants;
import com.customs.network.fdapn.validations.objects.commodity.ProductDetails;

import java.util.*;

import org.springframework.stereotype.Component;

import static com.customs.network.fdapn.validations.utils.ErrorUtils.createValidationError;

@Component
public class RADCommodityValidator extends CommonValidations implements CommodityValidator,SegmentValidator{
    private static final String PROGRAMME_CODE = "RAD";
    private final ConditionalValidator conditionalValidator;

    public RADCommodityValidator(RADCommodityConstants radCommodityConstants) {
        this.conditionalValidator = radCommodityConstants;
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

    @Override
    public void validateProductConstituentElement(ValidationContext context) {

    }

    //additional validation for affirmation of compliance ----------------------------------------------------------------
    @Override
    public Set<String> validateAffirmationOfCompliance(ValidationContext context) {
        Set<String> seenAocCodes = super.validateAffirmationOfCompliance(context);
        if (!seenAocCodes.isEmpty()) {
            Map<String, Set<String>> aocDependencies = context.conditionalValidator().getAocDependencies();
            if (!aocDependencies.isEmpty()) {
                for (Map.Entry<String, Set<String>> entry : aocDependencies.entrySet()) {
                    String key = entry.getKey();
                    Set<String> values = entry.getValue();
                    if (seenAocCodes.contains(key)) {
                        boolean hasAnyRequiredValue = values.stream().anyMatch(seenAocCodes::contains);
                        if (!hasAnyRequiredValue) {
                            context.errors().add(createValidationError(
                                    context.productCode(),
                                    "affirmationOfCompliance",
                                    String.format("When %s is present, at least one of %s is required", key, values),
                                    seenAocCodes.toString(),
                                    values.toString()
                            ));
                        }
                    }
                }
            }

        }
        return seenAocCodes;
    }

    @Override
    public ConditionalValidator getConditionalValidator(){
        return conditionalValidator;
    }
}
