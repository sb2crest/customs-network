package com.customs.network.fdapn.validations;

import com.customs.network.fdapn.validations.productdto.AffirmationOfCompliance;
import com.customs.network.fdapn.validations.productdto.PartyDetails;
import com.customs.network.fdapn.model.ValidationError;
import io.micrometer.common.util.StringUtils;

import java.util.*;

public class DataValidator {
    private DataValidator(){}
    public static List<ValidationError> validatePartyDetails(List<PartyDetails> partyDetailsList,String programCode) {
        if (partyDetailsList== null || partyDetailsList.isEmpty()){
            return Collections.emptyList();
        }
        List<ValidationError> validationErrors=new ArrayList<>();
        if("FOO".equalsIgnoreCase(programCode)){
            validationErrors.addAll(verifyPartyDetailsWhenProgramCodeIsFOO(partyDetailsList,programCode));
        }else{
            validationErrors.addAll(verifyPartyDetailsForOtherProgramCodes(partyDetailsList,programCode));
        }
        return validationErrors;
    }

    private static List<ValidationError> verifyPartyDetailsWhenProgramCodeIsFOO(List<PartyDetails> partyDetails,String programCode){

        return Collections.emptyList();
    }
    private static List<ValidationError> verifyPartyDetailsForOtherProgramCodes(List<PartyDetails> partyDetailsList, String programCode) {
        List<ValidationError> validationErrors = new ArrayList<>();
        Set<String> mandatoryPartyTypes = new HashSet<>(ValidationConstants.getMandatoryPartyTypes(programCode));
        Set<String> seenPartyTypes = new HashSet<>();
        int objectIndex=0;
        String objectName="partyDetails";
        for (PartyDetails partyDetails : partyDetailsList) {

            String country = partyDetails.getCountry();
            String stateOrProvince = partyDetails.getStateOrProvince();
            String individualQualifier = partyDetails.getIndividualQualifier();
            String partyType = partyDetails.getPartyType();
            String postalCode = partyDetails.getPostalCode();
            String partyIdentifierType= partyDetails.getPartyIdentifierType();
            String partyIdentifierNumber= partyDetails.getPartyIdentifierNumber();

            if (!ValidationConstants.isValidIndividualQualifierCode(individualQualifier)) {
                validationErrors.add(createValidationError(objectName+"["+objectIndex+"].individualQualifier", "Invalid individualQualifier code provided", individualQualifier));
            }
            if (!seenPartyTypes.add(partyType)) {
                validationErrors.add(createValidationError(objectName+"["+objectIndex+"].partyType", "Duplicate party type provided", partyType));
            }
            validationErrors.addAll(Helper.validatePartyType(programCode, partyType,objectName,objectIndex));
            validationErrors.addAll(Helper.validateCountryAndStateCode(country, stateOrProvince,postalCode,objectName,objectIndex));
            validationErrors.addAll(Helper.validatePartyIdentifierTypeAndNumber(partyIdentifierType,partyIdentifierNumber,objectName,objectIndex));
            objectIndex++;
        }

        Set<String> missingMandatoryTypes = new HashSet<>(mandatoryPartyTypes);
        missingMandatoryTypes.removeAll(seenPartyTypes);
        if (!missingMandatoryTypes.isEmpty()) {
            validationErrors.add(createValidationError(objectName, "Missing Mandatory party types", seenPartyTypes.toString(), missingMandatoryTypes.toString()));
        }

        return validationErrors;
    }


    public static List<ValidationError> validateAffirmationOfCompliance(List<AffirmationOfCompliance> compliance, String programCode){
        if (compliance== null || compliance.isEmpty()){
            return Collections.emptyList();
        }
        List<ValidationError> validationErrors=new ArrayList<>();
        Set<String>seenAoc=new HashSet<>();
        for (AffirmationOfCompliance affirmationOfCompliance : compliance){
            String aoc = affirmationOfCompliance.getAffirmationComplianceCode();
            String aocq = affirmationOfCompliance.getAffirmationComplianceQualifier();
            if(!seenAoc.add(aoc)){
                validationErrors.add(createValidationError("affirmationComplianceCode","affirmationComplianceCode can not repeat in the list",aoc));
            }
            if(StringUtils.isNotBlank(aoc)){
                if(!ValidationConstants.isValidAOCCode(programCode,affirmationOfCompliance.getAffirmationComplianceCode())){
                    validationErrors.add(createValidationError("affirmationComplianceCode",
                            "Invalid affirmationComplianceCode code provided for the program code "+
                                    programCode,affirmationOfCompliance.getAffirmationComplianceCode()));
                }else{
                    boolean isValidSyntax = ValidationConstants.validateAOCQSyntax(programCode,aoc,aocq);
                    if(!isValidSyntax){
                        validationErrors.add(createValidationError("affirmationComplianceQualifier",
                                DataViolationMessages.getAOCQSyntaxErrorMessage(aoc),affirmationOfCompliance.getAffirmationComplianceQualifier()));
                    }
                }
            }
        }
        return validationErrors;
    }

    public static ValidationError createValidationError(String fieldName, String errorMessage, Object fieldValue) {
        ValidationError validationError = new ValidationError();
        validationError.setFieldName(fieldName);
        validationError.setMessage(errorMessage);
        validationError.setActual(fieldValue);
        return validationError;
    }
    public static ValidationError createValidationError(String fieldName, String errorMessage, Object fieldValue, Object expectedValue) {
        ValidationError validationError = new ValidationError();
        validationError.setFieldName(fieldName);
        validationError.setMessage(errorMessage);
        validationError.setActual(fieldValue);
        validationError.setExpected(expectedValue);
        return validationError;
    }
}
