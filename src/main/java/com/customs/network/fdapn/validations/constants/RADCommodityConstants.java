package com.customs.network.fdapn.validations.constants;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class RADCommodityConstants extends GeneralValidationConstants implements ConditionalValidator {
    private static final Set<String> PROCESSING_CODES = Set.of("REP");
    private static final Set<String> INTENDED_USE_CODES = Set.of("085.000", "090.000", "100.000", "110.000", "120.000",
            "130.000", "140.000", "150.000", "155.000", "170.000", "180.000", "970.000", "980.000", "UNK");
    private static final Set<String> OPTIONAL_PARTY_TYPES = Set.of("PK");
    private static final List<Integer> INDUSTRY_CODE_RANGE = List.of(94, 97);
    private static final Map<String,Set<String>> VALID_AOC_CODES = Map.of(
            "optional",Set.of("RA1","RA2","RA3","RA4","RA5","RA6","RA7","RB1","RB2","RC1","RC2","RD1","RD2","RD3","ACC","ANC","MDL","ERR","IFE","CCM")
    );
    private static final Map<String,Set<String>> AOC_CODE_DEPENDENCIES =  Map.of(
            "ACC", Set.of("RB1"),
            "ANC", Set.of("RB1"),
            "RB1", Set.of("ACC", "ANC")
    );
    private static final Map<String, String> VALID_AOCQ_SYNTAX = Map.of(
            "RA1","^(0[1-9]|1[0-2])/(19|20)\\d{2}$",
            "ACC", "^(.{7}|.{11})$",
            "ANC", "^(.{7}|.{11})$"
    );
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
        return PROCESSING_CODES.contains(processingCode.toUpperCase());
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
        if (!productCode.matches(getProductCodeStructure())) {
            return false;
        }
        int industryCode = Integer.parseInt(productCode.substring(0, 2));
        return industryCode >= INDUSTRY_CODE_RANGE.get(0) && industryCode <= INDUSTRY_CODE_RANGE.get(1);
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
    public boolean isRequiredEveryConstituentElementFields(String intendedUseCode) {
        return false;
    }

    @Override
    public String getAOCQSynatx(String aoc) {
        if (VALID_AOCQ_SYNTAX.containsKey(aoc.toUpperCase()))
            return VALID_AOCQ_SYNTAX.get(aoc.toUpperCase());
        return null;
    }

    @Override
    public Set<String> getConditionalPartyTypes(String processingCode) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getOptionalPartyTypes() {
        return OPTIONAL_PARTY_TYPES;
    }

    @Override
    public String getPartyIdentifierNumberSyntax(String partyIdentifierType) {
        return null;
    }

    @Override
    public Map<String, Set<String>> getScenarioBasedAocCode(String processingCode, String intendedUseCode) {
        return VALID_AOC_CODES;
    }
    @Override
    public Map<String,Set<String>> getAocDependencies(){
        return AOC_CODE_DEPENDENCIES;
    }
}
