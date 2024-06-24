package com.customs.network.fdapn.validations;

import com.customs.network.fdapn.model.ValidationError;
import io.micrometer.common.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.customs.network.fdapn.validations.DataValidator.createValidationError;

public class Helper {
    private Helper(){}
    public static List<ValidationError> validatePartyType(String programCode, String partyType,String objectName,int objectIndex) {
        List<ValidationError> validationErrors = new ArrayList<>();
        if ( StringUtils.isBlank(partyType) || ValidationConstants.isValidPartyType(programCode, partyType)) {
            return validationErrors;
        } else {
            validationErrors.add(createValidationError(objectName+"["+objectIndex+"].partyType", "Invalid Party Type for the program code "+programCode, partyType));
        }
        return validationErrors;
    }

    public static List<ValidationError> validateCountryAndStateCode(String countryCode, String stateOrProvince,String postalCode,String objectName,int objectIndex) {
        List<ValidationError> validationErrors=new ArrayList<>();
        if(StringUtils.isNotBlank(countryCode)){
            if(!ValidationConstants.isValidCountryCode(countryCode)){
                validationErrors.add(createValidationError(objectName+"["+objectIndex+"].country","Invalid country code provided",countryCode));
            }else{
                validationErrors.addAll(checkStateOrProvince(countryCode,stateOrProvince,postalCode,objectName,objectIndex));
            }
        }
        return validationErrors;
    }
    private static List<ValidationError> checkStateOrProvince(String countryCode,String stateOrProvince,String postalCode,String objectName,int objectIndex){
        List<ValidationError> validationErrors=new ArrayList<>();
        if(StringUtils.isNotBlank(stateOrProvince)){
            if(!ValidationConstants.isValidStateCode(countryCode,stateOrProvince)){
                validationErrors.add(createValidationError(objectName+"["+objectIndex+"].stateOrProvince",
                        "State code is not valid for the country "+countryCode,stateOrProvince));
            }
        }else{
            if("US".equals(countryCode) || "CA".equals(countryCode)){
                validationErrors.add(createValidationError(objectName+"["+objectIndex+"].stateOrProvince",
                        "State code is mandatory for the country "+countryCode,stateOrProvince));
                if(StringUtils.isBlank(postalCode)){
                    validationErrors.add(createValidationError("postalCode",
                            "Postal code is mandatory for the country "+countryCode,postalCode));
                }
            }
        }
        return validationErrors;
    }
    public static List<ValidationError> validatePartyIdentifierTypeAndNumber(String partyIdentifierType, String partyIdentifierNumber,String objectName,int objectIndex) {
        List<ValidationError> validationErrors=new ArrayList<>();
        if(StringUtils.isBlank(partyIdentifierType) && StringUtils.isBlank(partyIdentifierNumber))
            return validationErrors;
        if(StringUtils.isBlank(partyIdentifierType)){
            validationErrors.add(createValidationError(objectName+"["+objectIndex+"].partyIdentifierType",
                    "Party identifier type is mandatory",partyIdentifierType));
        }
        else if(StringUtils.isBlank(partyIdentifierNumber)){
            validationErrors.add(createValidationError(objectName+"["+objectIndex+"].partyIdentifierNumber",
                    "Party identifier number is mandatory",partyIdentifierNumber));
        }else{
            if(ValidationConstants.isValidPartyIdentifierType(partyIdentifierType)){
                String syntax=ValidationConstants.getPartyIdentifierNumberSyntax(partyIdentifierType);
                if(!partyIdentifierNumber.matches(syntax)){
                    validationErrors.add(createValidationError(objectName+"["+objectIndex+"].partyIdentifierNumber",
                            DataViolationMessages.getPartyIdentifierNumberErrorMessage(partyIdentifierType),partyIdentifierNumber));
                }
            }else {
                validationErrors.add(createValidationError(objectName+"["+objectIndex+"].partyIdentifierType",
                        "Invalid party identifier type provided",partyIdentifierType));
            }
        }
        return validationErrors;
    }
}
