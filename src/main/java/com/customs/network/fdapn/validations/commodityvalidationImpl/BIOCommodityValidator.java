package com.customs.network.fdapn.validations.commodityvalidationImpl;

import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.validations.CommodityValidator;
import com.customs.network.fdapn.validations.constants.BIOCommodityConstants;
import com.customs.network.fdapn.validations.constants.ConditionalValidator;
import com.customs.network.fdapn.validations.objects.ProductDetails;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.customs.network.fdapn.validations.utils.ErrorUtils.createValidationError;

@Component
public class BIOCommodityValidator implements CommodityValidator {
    private static final String PROGRAMME_CODE = "BIO";
    private final ConditionalValidator conditionalValidator;

    public BIOCommodityValidator(BIOCommodityConstants bioCommodityConstants) {
        this.conditionalValidator = bioCommodityConstants;
    }

    @Override
    public List<ValidationError> validate(ProductDetails productDetails) {
        List<ValidationError> errors = new ArrayList<>();
        validatePGAIdentifier(productDetails,errors);

        return errors;
    }

    private void validatePGAIdentifier(ProductDetails productDetails, List<ValidationError> errors) {
        String productCode=productDetails.getProductCodeNumber();
        String processingCode=productDetails.getGovernmentAgencyProcessingCode();
        String intendedUseCode=productDetails.getIntendedUseCode();
        String disclaimer = productDetails.getDisclaimer();
        String intendedUseCodeDescription=productDetails.getIntendedUseDescription();
        String correctionIndicator=productDetails.getCorrectionIndicator();
        if(StringUtils.isBlank(disclaimer)){
            if(StringUtils.isNotBlank(processingCode) && !conditionalValidator.isValidProcessingCode(processingCode)){
                errors.add(createValidationError(productCode,"governmentAgencyProcessingCode","Provided governmentAgencyProcessingCode is not valid for the program code "+PROGRAMME_CODE,processingCode));
            }
            if(StringUtils.isNotBlank(intendedUseCode) && !conditionalValidator.isValidIntendedUseCode(intendedUseCode)){
                errors.add(createValidationError(productCode,"intendedUseCode","Provided intendedUseCode is not valid for the program code "+PROGRAMME_CODE,intendedUseCode));
            }
        }

    }


    @Override
    public void initialize() {

    }
}
