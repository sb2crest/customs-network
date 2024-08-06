package com.customs.network.fdapn.validations.commodityvalidationimpl;

import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.validations.CommodityValidator;
import com.customs.network.fdapn.validations.constants.ConditionalValidator;
import com.customs.network.fdapn.validations.constants.DEVCommodityConstants;
import com.customs.network.fdapn.validations.objects.commodity.EntityAddress;
import com.customs.network.fdapn.validations.objects.commodity.EntityData;
import com.customs.network.fdapn.validations.objects.commodity.EntityDetails;
import com.customs.network.fdapn.validations.objects.commodity.ProductDetails;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.customs.network.fdapn.validations.utils.ErrorUtils.createValidationError;

@Component
public class DEVCommodityValidator extends CommonValidations implements CommodityValidator,SegmentValidator {
    private static final String PROGRAMME_CODE = "DEV";
    private final ConditionalValidator conditionalValidator;

    public DEVCommodityValidator(DEVCommodityConstants devCommodityConstants) {
        this.conditionalValidator = devCommodityConstants;
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
        //no need to validate
    }

    //additional validation for affirmation of compliance
    @Override
    public Set<String> validateAffirmationOfCompliance(ValidationContext context) {
        Set<String> seenAocCodes = super.validateAffirmationOfCompliance(context);
        if (!seenAocCodes.isEmpty()) {
            String processingCode = context.productDetails().getGovernmentAgencyProcessingCode();
            if (StringUtils.isNotBlank(processingCode)) {
                checkAocDependencies(context,processingCode,seenAocCodes);
            }
        }
        return seenAocCodes;
    }

    private void checkAocDependencies(ValidationContext context,String processingCode,Set<String> seenAocCodes){
        Map<String, Set<String>> aocDependencies = context.conditionalValidator().getAocDependencies(processingCode);
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


    //additional validation for party details --------------------------------
    @Override
    public Set<String> validatePartyDetails(ValidationContext context) {
        super.validatePartyDetails(context);
        List<EntityDetails> partyDetails = context.productDetails().getPartyDetails();
        if (partyDetails == null || partyDetails.isEmpty())
            return Collections.emptySet();
        String intendedUSeCode = context.productDetails().getIntendedUseCode();
        for (EntityDetails entityDetails : partyDetails) {
            EntityData entityData = entityDetails.getEntityData();
            EntityAddress entityAddress = entityDetails.getEntityAddress();
            String partyType = entityData.getPartyType();
            String countryCode = entityAddress.getCountry();
            if (context.conditionalValidator().isValidCountryCode(countryCode) && context.conditionalValidator().isValueExcluded(intendedUSeCode, partyType, countryCode)) {
                context.errors().add(createValidationError(context.productCode(), "country",
                        String.format("The country code entered for the party %s is invalid for the intendedUseCode %s", partyType, intendedUSeCode), countryCode));
            }
        }
        return Collections.emptySet();
    }

    @Override
    public ConditionalValidator getConditionalValidator(){
        return conditionalValidator;
    }
    // Implement validation rules specific to development commodities
}
