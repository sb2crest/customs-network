package com.customs.network.fdapn.validations.commodityvalidationimpl;

import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.validations.CommodityValidator;
import com.customs.network.fdapn.validations.constants.ConditionalValidator;
import com.customs.network.fdapn.validations.constants.FOOCommodityConstants;
import com.customs.network.fdapn.validations.objects.commodity.AffirmationOfCompliance;
import com.customs.network.fdapn.validations.objects.commodity.ProductDetails;
import io.micrometer.common.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.customs.network.fdapn.utils.UtilMethods.isNullOrEmptyCollection;
import static com.customs.network.fdapn.validations.utils.ErrorUtils.createValidationError;

public class FOOCommodityValidator extends CommonValidations implements CommodityValidator,SegmentValidator {
    private final ConditionalValidator conditionalValidator;
    private static final String PROGRAMME_CODE = "FOO";

    public FOOCommodityValidator(FOOCommodityConstants fooCommodityConstants) {
        this.conditionalValidator = fooCommodityConstants;
    }
    @Override
    public List<ValidationError> validate(ProductDetails productDetails) {
        List<ValidationError> errors = new ArrayList<>();
        String productCode = productDetails.getProductCodeNumber();
        ValidationContext context =new ValidationContext(productCode,errors,conditionalValidator,PROGRAMME_CODE,productDetails,null);
        validatePGAIdentifier(context);
        validateProductIdentifier(context);
        validateProductOrigin(context);
        validateProductTradeNames(context);
        validatePartyDetails(context);
        validateAffirmationOfCompliance(context);
        return errors;
    }
    @Override
    public Set<String> validatePartyDetails(ValidationContext context){
        Set<String> seenPartyTypes = super.validatePartyDetails(context);
        String processingCode = context.productDetails().getGovernmentAgencyProcessingCode();
        if(StringUtils.isNotBlank(processingCode)){
            Set<String> conditionalPartyTypes = context.conditionalValidator().getConditionalPartyTypes(processingCode);
            if(!conditionalPartyTypes.isEmpty()){
                boolean hasConditionalPartyType = conditionalPartyTypes.stream()
                       .anyMatch(seenPartyTypes::contains);
                if(!hasConditionalPartyType){
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
        List<AffirmationOfCompliance> affirmationOfCompliances = context.productDetails().getAffirmationOfCompliance();
        if(!isNullOrEmptyCollection(affirmationOfCompliances)){
           for (AffirmationOfCompliance compliance : affirmationOfCompliances){
               String aocCode = compliance.getAffirmationComplianceCode();
               String aocq =compliance.getAffirmationComplianceQualifier();
               validateIndividualAffirmation(aocCode,aocq,context);

           }
        }
        return Collections.emptySet();
    }
    private void validateIndividualAffirmation(String aocCode, String aocq,ValidationContext context) {
        if(StringUtils.isNotBlank(aocCode) && !context.conditionalValidator().isValidAOCCode(aocCode)){
            context.errors().add(createValidationError(context.productCode(), "affirmationComplianceCode",
                    "Invalid affirmation compliance code provided ", aocCode));
        }else{
            String syntax = context.conditionalValidator().getAOCQSynatx(aocCode);
            if(syntax != null){
                if(StringUtils.isNotBlank(aocq) && !aocq.matches(syntax)){
                    //add error message
                }else if(StringUtils.isBlank(aocq)){
                    context.errors().add(createValidationError(context.productCode(), "affirmationComplianceQualifier",
                            "Affirmation of Compliance Qualifier is required for the affirmationOfComplianceCode " + aocCode, null, null));
                }
            }
        }
    }


    @Override
    public ConditionalValidator getConditionalValidator(){
        return conditionalValidator;
    }

    @Override
    public void validateProductConstituentElement(ValidationContext context) {
        //no need to validate
    }
}
