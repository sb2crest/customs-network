package com.customs.network.fdapn.validations.constants;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class DRUCommodityConstants extends GeneralValidationConstants implements ConditionalValidator, ProductCodeValidator {
    private static final Set<String> VALID_PROCESSING_CODES = Set.of("PRE", "OTC", "INV", "PHN", "RND", "804");
    private static final Set<String> PROCESSING_CODES_REQUIRED_CONSTITUENT_ELEMENT = Set.of("PRE", "OTC", "INV", "RND");
    private static final Set<String> REPEATABLE_PARTY_TYPES = Set.of("GD");
    private static final String VALID_TEMPERATURE_INDICATOR = "X";
    private static final Map<String, Map<String, String>> CONDITIONAL_COUNTRY_CODES = Map.of("804", Map.of("DEQ", "CA"));
    private static final Set<String> VALID_DEGREE_TYPES =Set.of("F","C","K");
    private static final Set<String> VALID_LOCATION_OF_TEMPERATURE_RECORDING = Set.of("A","B","C");
    private static final Set<String> LOT_NUMBER_REQUIRED_PROCESSING_CODE  =  Set.of("804");
    private static final Set<String> ACTIVE_PHARMACEUTICAL_USE_CODES = Set.of("150.007", "150.013", "150.017", "180.009", "180.017", "180.018", "180.026", "920.000", "980.000");
    private static final Map<String, Set<String>> VALID_INTENDED_USE_CODES = Map.of("PRE", Set.of("080.012", "100.000", "155.009", "920.000", "970.000", "980.000", "150.007", "150.013", "150.017", "UNK"),
            "OTC", Set.of("100.000", "130.000", "155.009", "920.000", "970.000", "150.007", "150.013", "150.017", "UNK"),
            "INV", Set.of("180.009", "180.026", "920.000", "UNK"),
            "RND", Set.of("180.017", "180.018", "UNK"),
            "804", Set.of("080.012", "UNK"));
    private static final Map<Set<String>, Set<String>> VALID_INDUSTRY_CODES = Map.of(Set.of("PRE", "OTC", "INV", "RND"), Set.of("54", "56", "58", "60", "61", "62", "63", "64", "65", "66"),
            Set.of("PHN"), Set.of("54", "55", "56", "58", "60", "61", "62", "63", "64", "65", "66"),
            Set.of("804"), Set.of("54", "56", "60", "61", "62", "63", "64", "65", "66"));

    private static final Map<String, Map<String, Set<String>>> VALID_SUBCLASS_CODES = Map.of(
            "PRE", Map.of(
                    "54", Set.of("F", "G"),
                    "56", Set.of("C", "D"), "58", Set.of("C", "D"), "60", Set.of("C", "D"),
                    "61", Set.of("C", "D"), "62", Set.of("C", "D"), "63", Set.of("C", "D"),
                    "64", Set.of("C", "D"), "65", Set.of("C", "D"), "66", Set.of("C", "D")
            ),
            "OTC", Map.of(
                    "54", Set.of("D", "E"),
                    "56", Set.of("A", "B"), "58", Set.of("A", "B"), "60", Set.of("A", "B"),
                    "61", Set.of("A", "B"), "62", Set.of("A", "B"), "63", Set.of("A", "B"),
                    "64", Set.of("A", "B"), "65", Set.of("A", "B"), "66", Set.of("A", "B")
            ),
            "INV", Map.of(
                    "54", Set.of("I"),
                    "56", Set.of("I"), "58", Set.of("I"), "60", Set.of("I"),
                    "61", Set.of("I"), "62", Set.of("I"), "63", Set.of("I"),
                    "64", Set.of("I"), "65", Set.of("I"), "66", Set.of("I")
            )
    );
    private static final Map<String, Map<String, String>> VALID_PROCESS_INDICATOR_CODE = Map.of(
            "PRE", Map.of("150.007", "S", "150.017", "S", "150.013", "T"),
            "OTC", Map.of("150.007", "S", "150.017", "S", "150.013", "T")
    );

    private static final Set<String> OPTIONAL_PARTY_TYPES = Set.of("SPO", "GD", "PK");
    private static final Map<String, Map<Set<String>, Map<String, Set<String>>>> SCENARIO_BASED_AOC_CODES = Map.of(
            "080.012", Map.of(Set.of("PRE"), Map.of("mandatory", Set.of("DA", "REG"), "anyOf", Set.of("DLS", "PLR"),"optional", Set.of("ERR")),
                    Set.of("804"), Map.of("mandatory", Set.of("DA", "DLS", "FSR", "PRN"), "optional", Set.of("REG","ERR"))),
            "150.007", Map.of(Set.of("PRE"), Map.of("mandatory", Set.of("DA", "REG", "DLS"),"optional", Set.of("ERR")),
                    Set.of("OTC"), Map.of("mandatory", Set.of("REG", "DLS"), "optional", Set.of("DA","ERR"))),
            "130.000", Map.of(Set.of("OTC"), Map.of("mandatory", Set.of("REG", "DLS"), "optional", Set.of("DA","ERR"))),
            "150.017", Map.of(Set.of("OTC", "PRE"), Map.of("mandatory", Set.of("REG", "DLS"), "optional", Set.of("DA", "LST", "PM#", "IDE","ERR"))),
            "150.009", Map.of(Set.of("OTC", "PRE"), Map.of("mandatory", Set.of("REG", "DLS"), "optional", Set.of("DA", "LST", "PM#", "IDE","ERR"))),
            "920.000", Map.of(Set.of("OTC", "PRE", "INV"), Map.of("optional", Set.of("DA", "REG", "DLS", "IND","ERR"))),
            "980.000", Map.of(Set.of("PRE"), Map.of("mandatory", Set.of("REG"), "anyOf", Set.of("DLS", "PLR"),"optional", Set.of("ERR"))),
            "150.013", Map.of(Set.of("PRE", "OTC"), Map.of("mandatory", Set.of("REG", "DLS"),"optional", Set.of("ERR"))),
            "180.009", Map.of(Set.of("INV"), Map.of("mandatory", Set.of("IND"),"optional", Set.of("ERR"))),
            "UNK",Map.of(Set.of("INV","PRE","OTC","804"),Map.of("optional",Set.of("DA","REG","DLS","FSR","PRN","ERR","LST","PM#","IDE","PLR")))
    );

    private static final Map<String, String> VALID_AOCQ_SYNTAX = Map.of(
            "DA","^\\d{6}$",
            "REG","^\\d{9}$",
            "DLS","^\\d{10}$",
            "IND","^\\d{6}$",
            "FSR","^\\d{9}$",
            "PRN","^\\d{6,10}$",
            "LST","^[ABCDEGLQR]\\d{6}$",
            "PM#","^(P\\d{6}|N\\d{4,6}|D\\d{6}|H\\d{6}|K\\d{6}|DEN\\d{6})$",
            "IDE","^(G\\d{6}|NSR)$"
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
                OPTIONAL_PARTY_TYPES.contains(partyType.toUpperCase());
    }

    @Override
    public boolean isPartyTypeRepeatable(String partyType) {
        if (REPEATABLE_PARTY_TYPES.isEmpty())
            return super.isPartyTypeRepeatable(partyType);
        return REPEATABLE_PARTY_TYPES.contains(partyType.toUpperCase());
    }

    @Override
    public boolean isValueExcluded(String processingCode, String partyType, String countryCode) {
        Map<String, String> countryMap = CONDITIONAL_COUNTRY_CODES.get(processingCode);
        if (countryMap == null) {
            return true;
        }
        String expectedCountryCode = countryMap.get(partyType);
        if (expectedCountryCode == null) {
            return true;
        }
        return expectedCountryCode.equalsIgnoreCase(countryCode);
    }

    @Override
    public boolean isValidIntendedUseCode(String intendedUseCode) {
        return true;
    }

    @Override
    public boolean isValidIntendedUseCode(String processingCode, String intendedUseCode) {
        if (VALID_INTENDED_USE_CODES.containsKey(processingCode.toUpperCase())) {
            return VALID_INTENDED_USE_CODES.get(processingCode.toUpperCase()).contains(intendedUseCode.toUpperCase());
        }
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
    public boolean isValidIndustryCode(String processingCode, String industryCode) {
        for (Map.Entry<Set<String>, Set<String>> entry : VALID_INDUSTRY_CODES.entrySet()) {
            Set<String> processingCodes = entry.getKey();
            Set<String> industryCodes = entry.getValue();
            if (processingCodes.contains(processingCode) && industryCodes.contains(industryCode)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isValidSubClassCode(String processingCode, String industryCode, String subClassCode) {
        Map<String, Set<String>> industryCodeLevel = VALID_SUBCLASS_CODES.get(processingCode.toUpperCase());
        if (industryCodeLevel == null) {
            return true;
        }
        Set<String> validSubClassCodes = industryCodeLevel.get(industryCode);
        return validSubClassCodes != null && validSubClassCodes.contains(subClassCode.toUpperCase());
    }

    @Override
    public boolean isValidProcessIndicatorCode(String processingCode, String intendedUseCode, String processIndicatorCode) {
        Map<String, String> intendedUseMap = VALID_PROCESS_INDICATOR_CODE.get(processingCode);
        if (intendedUseMap == null) {
            return true;
        }
        String expectedIndicatorCode = intendedUseMap.get(intendedUseCode);
        return expectedIndicatorCode == null || expectedIndicatorCode.equals(processIndicatorCode.toUpperCase());
    }

    @Override
    public boolean isTBNRequired(String processingCode) {
        return false;
    }

    @Override
    public boolean isConstituentElementRequired(String processingCode) {
        return PROCESSING_CODES_REQUIRED_CONSTITUENT_ELEMENT.contains(processingCode.toUpperCase());
    }

    @Override
    public boolean isRequiredEveryConstituentElementFields(String intendedUseCode) {
        return ACTIVE_PHARMACEUTICAL_USE_CODES.contains(intendedUseCode.toUpperCase());
    }
    @Override
    public boolean isValidDegreeType(String degreeType){
       return VALID_DEGREE_TYPES.contains(degreeType.toUpperCase());
    }
    @Override
    public boolean isValidLocationOfTemperatureRecording(String locationOfTemperatureRecording){
        return VALID_LOCATION_OF_TEMPERATURE_RECORDING.contains(locationOfTemperatureRecording.toUpperCase());
    }

    @Override
    public boolean isValidTemperatureIndicator(String temperatureIndicator){
        return VALID_TEMPERATURE_INDICATOR.equalsIgnoreCase(temperatureIndicator);
    }
    @Override
    public boolean isLotNumberRequired(String processingCode){
        return LOT_NUMBER_REQUIRED_PROCESSING_CODE.contains(processingCode.toUpperCase());
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
        return Collections.emptySet();
    }

    @Override
    public String getPartyIdentifierNumberSyntax(String partyIdentifierType) {
        return null;
    }

    @Override
    public Map<String, Set<String>> getScenarioBasedAocCode(String processingCode, String intendedUseCode) {
        Map<Set<String>, Map<String, Set<String>>> intendedUseCodeLevel = SCENARIO_BASED_AOC_CODES.getOrDefault(intendedUseCode.toUpperCase(), Collections.emptyMap());
        for (Map.Entry<Set<String>, Map<String, Set<String>>> entry : intendedUseCodeLevel.entrySet()) {
            Set<String> processingCodeSet = entry.getKey();
            if (processingCodeSet.contains(processingCode.toUpperCase())) {
                return entry.getValue();
            }
        }
        return Collections.emptyMap();
    }
}
