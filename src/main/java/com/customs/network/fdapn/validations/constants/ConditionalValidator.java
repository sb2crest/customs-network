package com.customs.network.fdapn.validations.constants;

import java.util.Set;

public interface ConditionalValidator {
    boolean isValidCountryCode(String countryCode);
    boolean isValidStateCode(String countryCode,String stateCode);
    boolean isValidProgramCode(String productCode);
    boolean isValidIndividualQualifierCode(String individualQualifierCode);
    boolean isValidAOCCode(String aocCode);
    boolean isValidateAOCQSyntax(String aoc, String aocq);
    boolean isValidProcessingCode(String processingCode);
    boolean isValidPartyType(String partyType);
    boolean isValidPartyIdentifierType(String partyIdentifierType);
    boolean isValidPartyIdentifierNumberSyntax(String partyIdentifierType, String partyIdentifierNumber);
    boolean isValidIntendedUseCode(String intendedUseCode);
    String getAOCQSynatx(String aoc);
    Set<String> getMandatoryPartyTypes();
    Set<String> getConditionalPartyTypes();
    Set<String> getOptionalPartyTypes();
    String getPartyIdentifierNumberSyntax(String partyIdentifierType);

}
