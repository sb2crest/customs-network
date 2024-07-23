package com.customs.network.fdapn.validations.constants;

import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;

import java.util.Map;
import java.util.Set;

@Component
public class COSCommodityConstants extends GeneralValidationConstants implements ConditionalValidator {
    private static final Set<String> OPTIONAL_PARTY_TYPE = Set.of("PK");
    private static final Set<String> VALID_INTENDED_USE_CODES = Set.of();
    private static final Set<String> VALID_PROCESSING_CODES = Set.of();
    private static final Set<String> VALID_INDUSTRY_CODES = Set.of("50", "53");
    private static final Set<String> MANDATORY_SOURCE_CODE = Set.of("39");
    private static final Map<String, Set<String>> VALID_AOC_CODES = Map.of("optional", Set.of("COS", "ERR"));
    private static final boolean IS_TBN_REQUIRED = false;

    private static final Map<String,String> AOCQ_SYNTAX = Map.of("COS","^(\\d{7}|\\d{10})$");

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
        if (VALID_PROCESSING_CODES.isEmpty())
            return true;
        return VALID_PROCESSING_CODES.contains(processingCode.toUpperCase());
    }

    @Override
    public boolean isValidPartyType(String partyType) {
        return getMandatoryPartyTypes().contains(partyType.toUpperCase()) ||
                getOptionalPartyTypes().contains(partyType.toUpperCase());
    }

    @Override
    public Set<String> getMandatorySourceCode() {
        return MANDATORY_SOURCE_CODE;
    }

    @Override
    public boolean isValidIntendedUseCode(String intendedUseCode) {
        if (VALID_INTENDED_USE_CODES.isEmpty())
            return true;
        return VALID_INTENDED_USE_CODES.contains(intendedUseCode.toUpperCase());
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
        if (StringUtils.isBlank(productCode))
            return false;
        return productCode.toUpperCase().matches(getProductCodeStructure()) &&
                VALID_INDUSTRY_CODES.contains(productCode.substring(0, 2));
    }

    @Override
    public boolean isTBNRequired(String processingCode) {
        return IS_TBN_REQUIRED;
    }

    @Override
    public boolean isConstituentElementRequired(String processingCode) {
        return false;
    }

    @Override
    public boolean isRequiredEveryConstituentElementFields(String intendedUseCode) {
        return false;
    }

    @Override
    public String getAOCQSynatx(String aoc) {
        if (AOCQ_SYNTAX.containsKey(aoc.toUpperCase()))
            return AOCQ_SYNTAX.get(aoc.toUpperCase());
        return null;
    }

    @Override
    public Set<String> getConditionalPartyTypes(String processingCode) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getOptionalPartyTypes() {
        return OPTIONAL_PARTY_TYPE;
    }

    @Override
    public String getPartyIdentifierNumberSyntax(String partyIdentifierType) {
        return null;
    }

    @Override
    public Map<String, Set<String>> getScenarioBasedAocCode(String processingCode, String intendedUseCode) {
        return VALID_AOC_CODES;
    }


}
