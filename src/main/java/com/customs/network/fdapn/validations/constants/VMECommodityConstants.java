package com.customs.network.fdapn.validations.constants;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;


@Component
public class VMECommodityConstants extends GeneralValidationConstants implements ConditionalValidator, ProductCodeValidator {
    private static final Set<String> VALID_PROCESSING_CODES = Set.of("ADR", "ADE");
    private static final Set<String> VALID_INTENDED_USE_CODES = Set.of("085.003", "100.000", "150.013", "150.020 ", "180.009", "180.018", "920.000", "970.000", "980.000", "UNK");
    private static final Set<String> IUC_MANDATORY_PROCESSING_CODES = Set.of("ADR");
    private static final Map<String, Set<String>> VALID_INDUSTRY_CODES = Map.of(
            "ADR", Set.of("54", "56", "58", "60", "61", "62", "63", "64", "65", "66", "67"),
            "ADE", Set.of("68")
    );
    private static final Map<String, Map<String, Set<String>>> VALID_SUB_CLASS_CODES = Map.of(
            "ADR", Map.of("54", Set.of("N", "R"))
    );
    private static final Set<String> CONSTITUENT_FORCE_CHECK = Set.of("ADR");
    private static final Set<String> VALID_OPTIONAL_PARTY_TYPES = Set.of("GD", "PK");
    private static final Map<String, Map<String, Map<String, Set<String>>>> AOC_USE_CASES = Map.of(
            "ADR", Map.of("185.003", Map.of("mandatory", Set.of("REG", "NDC"), "optional", Set.of("VFL", "VFD", "ERR"), "anyOf", Set.of("VNA", "VAN")),
                    "150.020", Map.of("mandatory", Set.of("REG", "NDC"), "optional", Set.of("ERR"), "anyOf", Set.of("VNA", "VAN")),
                    "150.013", Map.of("mandatory", Set.of("REG", "NDC"), "optional", Set.of("ERR")),
                    "980.000", Map.of("mandatory", Set.of("REG", "NDC"), "optional", Set.of("ERR", "VFD", "VFL", "VAN", "VNA")),
                    "180.009", Map.of("mandatory", Set.of("VIN"), "optional", Set.of("ERR")),
                    "180.018", Map.of("optional", Set.of("EER", "VIN")),
                    "UNK",Map.of("optional", Set.of("REG","NDC","ERR","VFL","VFD","VNA","VAN","VIN"))
            )
    );
    private static final Map<String, String> VALID_AOCQ_SYNTAX = Map.of(
            "REG","^\\d{9}$",
            "VAN","^\\d{6}$",
            "VIN","^\\d{6}$",
            "VNA","^\\d{6}$",
            "NDC","^\\d{10}$",
            "VFL","^.{7}$"
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
        return VALID_PROCESSING_CODES.contains(processingCode.toUpperCase());
    }

    @Override
    public boolean isValidPartyType(String partyType) {
        return getMandatoryPartyTypes().contains(partyType.toUpperCase()) ||
                getOptionalPartyTypes().contains(partyType.toUpperCase());
    }

    @Override
    public boolean isValidIntendedUseCode(String intendedUseCode) {
        return VALID_INTENDED_USE_CODES.contains(intendedUseCode.toUpperCase());
    }

    @Override
    public boolean isValidIntendedUseCode(String processingCode, String intendedUseCode) {
        return false;
    }

    @Override
    public boolean isRequiredIntendedUseCode(String processingCode) {
        return IUC_MANDATORY_PROCESSING_CODES.contains(processingCode.toUpperCase());
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
    public boolean isRequiredEveryConstituentElementFields(String processingCode) {
        return CONSTITUENT_FORCE_CHECK.contains(processingCode.toUpperCase());
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
        return VALID_OPTIONAL_PARTY_TYPES;
    }

    @Override
    public String getPartyIdentifierNumberSyntax(String partyIdentifierType) {
        return null;
    }

    @Override
    public Map<String, Set<String>> getScenarioBasedAocCode(String processingCode, String intendedUseCode) {
        Map<String, Map<String, Set<String>>> iucLevel = AOC_USE_CASES.get(processingCode);
        if (iucLevel != null) {
            Map<String, Set<String>> aocLevel = iucLevel.get(intendedUseCode.toUpperCase());
            if (aocLevel != null) {
                return aocLevel;
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public boolean isValidIndustryCode(String processingCode, String industryCode) {
        Set<String> validIndustry = VALID_INDUSTRY_CODES.get(processingCode.toUpperCase());
        if (validIndustry != null) {
            return validIndustry.contains(industryCode.toUpperCase());
        }
        return true;
    }

    @Override
    public boolean isValidSubClassCode(String processingCode, String industryCode, String subClassCode) {
        Map<String, Set<String>> industryCodeLevel = VALID_SUB_CLASS_CODES.get(processingCode.toUpperCase());
        if (industryCodeLevel == null) {
            return true;
        }
        Set<String> subClassCodeSet = industryCodeLevel.get(industryCode.toUpperCase());
        return subClassCodeSet != null && subClassCodeSet.contains(subClassCode.toUpperCase());
    }

    @Override
    public boolean isValidProcessIndicatorCode(String processingCode, String intendedUseCode, String processIndicatorCode) {
        return false;
    }
}
