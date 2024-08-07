package com.customs.network.fdapn.validations.constants;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class FOOCommodityConstants extends GeneralValidationConstants implements ConditionalValidator {
    private static final Set<String> VALID_PROCESSING_CODES = Set.of("NSF", "PRO", "FEE", "ADD", "DSU");
    private static final Set<String> INTENDED_USE_CODES = Set.of("260.000", "015.000", "210.000");
    private static final Map<String, Set<String>> MANDATORY_SOURCE_TYPE_CODES = Map.of();
    private static final Set<String> MANDATORY_PARTY_TYPES = Set.of("PNS", "PNT", "DEQ");
    private static final Set<String> OPTIONAL_PARTY_TYPES = Set.of("LG", "FSV", "FD1", "UC", "PK", "DFP");
    private static final Map<Set<String>, Set<String>> CONDITIONAL_PARTY_TYPES = Map.of(
            Set.of("PRO", "ADD", "DSU"), Set.of("MF"),
            Set.of("NSF", "FEE"), Set.of("DFI", "FDC")
    );
    private static final Set<String> MANDATORY_INDIVIDUAL_QUALIFIERS = Set.of("PNS", "PNT");
    private static final Set<String> ALL_VALID_INDIVIDUAL_QUALIFIERS = Set.of("PNS", "PNT", "FSV", "FD1", "PK");
    private static final Set<String> AOC_CODES = Set.of(
            "FME", "RNO", "CAN", "VFT", "VES", "PFR", "FCE", "SID", "VOL", "FSX", "RNE",
            "SFR", "UFR", "IFR", "TFR", "ORN", "SRN", "CFR", "GFR", "LFR",
            "CIN", "ERR", "FAP", "FCC", "IBP", "IFE", "PKC"+
            "AIN", "JIF", "SIF", "VQI", "REG", "VFL", "VFD");
    private static final Map<String,String> AOC_SYNTAX = Map.ofEntries(
            Map.entry("FME","^(\\d[A-Za-z]|[A-Za-z])$"),
            Map.entry("PFR","^\\d{11}$"),
            Map.entry("FCE","^\\d{5}$"),
            Map.entry("SID","^\\d{11}$"),
            Map.entry("SFR","^\\d{11}$"),
            Map.entry("UFR","^\\d{11}$"),
            Map.entry("IFR","^\\d{11}$"),
            Map.entry("TFR","^\\d{11}$"),
            Map.entry("ORN","^\\d{11}$"),
            Map.entry("SRN","^\\d{11}$"),
            Map.entry("CFR","^\\d{11}$"),
            Map.entry("GFR","^\\d{11}$"),
            Map.entry("LFR","^\\d{11}$"),
            Map.entry("CIN","^.{0,30}$"),
            Map.entry("FAP","^\\d{6}$"),
            Map.entry("FCC","^(\\d{2} \\d{3} \\d{2}|\\d{2} \\d{3} \\d{3})$"),
            Map.entry("AIN","^(\\d{6}|\\d{8}|E\\d{7})$"),
            Map.entry("JIF","^\\d{1,10}$"),
            Map.entry("SIF","^\\d{1,10}$"),
            Map.entry("VQI","^\\d{5}$"),
            Map.entry("REG","^\\d{9}$"),
            Map.entry("VFL","^[\\w\\W]{7}$")
    );

    @Override
    public boolean isValidAOCCode(String aocCode) {
         return AOC_CODES.contains(aocCode.toUpperCase());
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
        return MANDATORY_PARTY_TYPES.contains(partyType.toUpperCase()) ||
                OPTIONAL_PARTY_TYPES.contains(partyType.toUpperCase()) ||
                CONDITIONAL_PARTY_TYPES.values()
                        .stream()
                        .anyMatch(val -> val.contains(partyType.toUpperCase()));
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
        if(AOC_SYNTAX.containsKey(aoc.toUpperCase())){
            return AOC_SYNTAX.get(aoc.toUpperCase());
        }
        return null;
    }

    @Override
    public Set<String> getConditionalPartyTypes(String processingCode) {
        for (Map.Entry<Set<String>, Set<String>> entry : CONDITIONAL_PARTY_TYPES.entrySet()) {
            if (entry.getKey().contains(processingCode.toUpperCase())) {
                return entry.getValue();
            }
        }
        return Collections.emptySet();
    }

    @Override
    public Set<String> getMandatoryPartyTypes() {
        return MANDATORY_PARTY_TYPES;
    }

    @Override
    public Set<String> getOptionalPartyTypes() {
        return OPTIONAL_PARTY_TYPES;
    }

    @Override
    public Set<String> getMandatorySourceCode(String processingCode) {
        return MANDATORY_SOURCE_TYPE_CODES.get(processingCode);
    }

    @Override
    public Set<String> getRequiredIndividualQualifier() {
        return MANDATORY_INDIVIDUAL_QUALIFIERS;
    }

    @Override
    public boolean isValidIndividualQualifierCode(String individualQualifierCode) {
        return ALL_VALID_INDIVIDUAL_QUALIFIERS.contains(individualQualifierCode.toUpperCase());
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
