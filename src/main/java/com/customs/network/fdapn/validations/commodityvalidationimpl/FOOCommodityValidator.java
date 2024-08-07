package com.customs.network.fdapn.validations.commodityvalidationimpl;

import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.validations.CommodityValidator;
import com.customs.network.fdapn.validations.DataViolationMessages;
import com.customs.network.fdapn.validations.constants.ConditionalValidator;
import com.customs.network.fdapn.validations.constants.FOOCommodityConstants;
import com.customs.network.fdapn.validations.objects.commodity.AffirmationOfCompliance;
import com.customs.network.fdapn.validations.objects.commodity.ProductDetails;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.customs.network.fdapn.utils.UtilMethods.isNullOrEmptyCollection;
import static com.customs.network.fdapn.validations.utils.ErrorUtils.createValidationError;

@Component
@Slf4j
public class FOOCommodityValidator extends CommonValidations implements CommodityValidator, SegmentValidator {
    private final ConditionalValidator conditionalValidator;
    private static final String PROGRAMME_CODE = "FOO";

    public FOOCommodityValidator(FOOCommodityConstants fooCommodityConstants) {
        this.conditionalValidator = fooCommodityConstants;
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

    //validate affirmation of Compliance
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
                    context.errors().add(createValidationError(context.productCode(), "affirmationComplianceQualifier - "+ aocCode, DataViolationMessages.getAOCQSyntaxErrorMessage(programCode, aocCode), aocq));
                } else if (StringUtils.isBlank(aocq)) {
                    context.errors().add(createValidationError(context.productCode(), "affirmationComplianceQualifier",
                            "Affirmation of Compliance Qualifier is required for the affirmationOfComplianceCode " + aocCode, null, null));
                }
            }
        }
        return isValidAoc;
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
