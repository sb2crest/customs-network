package com.customs.network.fdapn.validations.utils;

import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.validations.constants.ConditionalValidator;
import com.customs.network.fdapn.validations.objects.ProductDetails;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static com.customs.network.fdapn.validations.utils.ErrorUtils.createValidationError;
@Slf4j
public class CommonValidations {
    public  void validatePGAIdentifier(ProductDetails productDetails, List<ValidationError> errors, ConditionalValidator conditionalValidator,String programCode) {
        String productCode=productDetails.getProductCodeNumber();
        String agancyCode=productDetails.getGovernmentAgencyCode();
        String processingCode=productDetails.getGovernmentAgencyProcessingCode();
        String intendedUseCode=productDetails.getIntendedUseCode();
        String disclaimer = productDetails.getDisclaimer();
        if(StringUtils.isBlank(disclaimer)){
            ifNotDisclaimed(productCode,processingCode,intendedUseCode,programCode,conditionalValidator,errors);
        }else if("A".equalsIgnoreCase(disclaimer) || "F".equalsIgnoreCase(disclaimer)){
            ifDisclaimed(productCode,processingCode,intendedUseCode,agancyCode,errors);
        }else{
            errors.add(createValidationError(productCode,"disclaimer","Invalid disclaimer provided ",disclaimer,"A/F/' '"));
        }
    }
    private  void ifDisclaimed(String productCode, String processingCode, String intendedUseCode, String agancyCode, List<ValidationError> errors){
        if(StringUtils.isNotBlank(agancyCode) && !processingCode.equalsIgnoreCase(agancyCode)){
            errors.add(createValidationError(productCode,"governmentAgencyProcessingCode","The line is disclaimed,Hence the value provided is not valid in this situation ",processingCode,agancyCode));
        }
        if(StringUtils.isNotBlank(intendedUseCode) &&!intendedUseCode.equalsIgnoreCase(agancyCode)){
            errors.add(createValidationError(productCode,"intendedUseCode","The line is disclaimed,Hence the value provided is not valid in this situation ",intendedUseCode,agancyCode));
        }
    }
    private  void ifNotDisclaimed(String productCode, String processingCode, String intendedUseCode, String programCode, ConditionalValidator conditionalValidator, List<ValidationError> errors){
        if(StringUtils.isNotBlank(processingCode) && !conditionalValidator.isValidProcessingCode(processingCode)){
            errors.add(createValidationError(productCode,"governmentAgencyProcessingCode","Provided governmentAgencyProcessingCode is not valid for the program code "+programCode,processingCode));
        }
        if(StringUtils.isNotBlank(intendedUseCode) && !conditionalValidator.isValidIntendedUseCode(intendedUseCode)){
            errors.add(createValidationError(productCode,"intendedUseCode","Provided intendedUseCode is not valid for the program code "+programCode,intendedUseCode));
        }
    }


}
