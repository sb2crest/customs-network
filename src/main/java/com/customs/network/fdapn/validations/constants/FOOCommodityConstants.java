package com.customs.network.fdapn.validations.constants;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class FOOCommodityConstants extends  GeneralValidationConstants implements ConditionalValidator{
    private static final Set<String> VALID_PROCESSING_CODES = Set.of("NSF", "PRO","FEE","ADD","DSU","CCW");
    private static final Set<String> INTENDED_USE_CODES = Set.of("260.000","015.000","210.000");
    private static final Map<String, Set<String>> MANDATORY_SOURCE_TYPE_CODES = Map.of();
    @Override
    public boolean isValidAOCCode(String aocCode) {
        return false;
    }

    @Override
    public boolean isValidAOCQSyntax(String aoc, String aocq) {
        return false;
    }

    @Override
    public boolean isValidProcessingCode(String processingCode) {
        return VALID_PROCESSING_CODES.contains(processingCode.toUpperCase());
    }

    @Override
    public boolean isValidPartyType(String partyType) {
        return false;
    }

    @Override
    public boolean isValidIntendedUseCode(String intendedUseCode) {
        return INTENDED_USE_CODES.contains(intendedUseCode.toUpperCase());
    }

    @Override
    public boolean isValidIntendedUseCode(String processingCode, String intendedUseCode) {
        return false;
    }

    @Override
    public boolean isValidProductCodeStructure(String productCode, String processingCode) {
        return false;
    }

    @Override
    public boolean isValidProductCodeStructure(String productCode) {
        return productCode.toUpperCase().matches(getProductCodeStructure());
    }

    @Override
    public boolean isTBNRequired(String processingCode) {
        return false;
    }

    @Override
    public boolean isConstituentElementRequired(String processingCode) {
        return false;
    }

    @Override
    public boolean isRequiredEveryConstituentElementFields(String code) {
        return false;
    }

    @Override
    public String getAOCQSynatx(String aoc) {
        return null;
    }

    @Override
    public Set<String> getConditionalPartyTypes(String processingCode) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getOptionalPartyTypes() {
        return Collections.emptySet();
    }
    public Set<String> getMandatorySourceCode(String processingCode){
        return MANDATORY_SOURCE_TYPE_CODES.get(processingCode);
    }

    @Override
    public String getPartyIdentifierNumberSyntax(String partyIdentifierType) {
        return null;
    }

    @Override
    public Map<String, Set<String>> getScenarioBasedAocCode(String processingCode, String intendedUseCode) {
        return Collections.emptyMap();
    }
}
