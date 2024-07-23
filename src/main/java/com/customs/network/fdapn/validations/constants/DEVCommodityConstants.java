package com.customs.network.fdapn.validations.constants;

import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class DEVCommodityConstants extends GeneralValidationConstants implements ConditionalValidator {
    private static final Set<String> VALID_PROCESSING_CODES = Set.of("RED", "NED");
    private static final Set<String> VALID_INTENDED_USE_CODES = Set.of("081.001", "081.002", "081.003", "081.004",
            "081.005", "081.006", "100.000", "110.000",
            "140.000", "081.007", "081.008", "170.000", "180.010", "180.014", "180.015", "920.001", "920.002",
            "940.000", "950.001", "950.002", "970.000", "970.001", "UNK");
    private static final Set<String> MANDATORY_PARTY_TYPES = Set.of("MF", "DEQ", "FD1", "DII", "DP");
    private static final Set<String> OPTIONAL_PARTY_TYPES = Set.of("PK");
    private static final Map<String, Map<Set<String>, Map<String, Set<String>>>> SCENARIO_BASED_AOC_CODES = Map.ofEntries(
            Map.entry("NED", Map.ofEntries(
                    Map.entry(Set.of("081.001", "081.002", "140.000"), Map.of(
                            "mandatory", Set.of("DEV", "DFE", "LST"),
                            "optional", Set.of("IRC", "LWC", "PM#", "DI", "ERR")
                    )),
                    Map.entry(Set.of("081.003"), Map.of(
                            "mandatory", Set.of("DDM", "DFE", "KIT", "LST"),
                            "optional", Set.of("IRC", "LWC", "PM#", "DI", "ERR")
                    )),
                    Map.entry(Set.of("081.004"), Map.of(
                            "mandatory", Set.of("DEV", "DFE", "KIT", "LST"),
                            "optional", Set.of("IRC", "LWC", "PM#", "DI", "ERR")
                    )),
                    Map.entry(Set.of("081.005"), Map.of(
                            "optional", Set.of("DA", "IND", "DEV", "DFE", "LST", "ERR", "DI")
                    )),
                    Map.entry(Set.of("081.007"), Map.of(
                            "mandatory", Set.of("CPT"),
                            "optional", Set.of("LST", "PM#", "ERR", "DI")
                    )),
                    Map.entry(Set.of("081.008"), Map.of(
                            "mandatory", Set.of("CPT"),
                            "optional", Set.of("DA", "IND", "ERR", "DI")
                    )),
                    Map.entry(Set.of("170.000"), Map.of(
                            "mandatory", Set.of("IFE"),
                            "optional", Set.of("DFE", "LST", "IRC", "LWC", "PM#", "DDM", "DI", "ERR")
                    )),
                    Map.entry(Set.of("180.010"), Map.of()),
                    Map.entry(Set.of("180.014"), Map.of()),
                    Map.entry(Set.of("180.015"), Map.of(
                            "mandatory", Set.of("IDE"),
                            "optional", Set.of("ERR", "DI")
                    )),
                    Map.entry(Set.of("920.001", "950.001"), Map.of(
                            "mandatory", Set.of("DDM", "LST"),
                            "optional", Set.of("DFE", "IRC", "LWC", "PM#", "ERR", "DI")
                    )),
                    Map.entry(Set.of("920.002"), Map.of(
                            "mandatory", Set.of("DFE", "LST", "DDM"),
                            "optional", Set.of("IRC", "LWC", "PM#", "ERR", "DI")
                    )),
                    Map.entry(Set.of("950.002"), Map.of(
                            "optional", Set.of("DDM", "DFE", "IRC", "LST", "PM#", "LWC", "ERR", "DI")
                    )),
                    Map.entry(Set.of("970.000"), Map.of(
                            "mandatory", Set.of("DEV", "DFE", "IFE", "LST"),
                            "optional", Set.of("ERR", "DI")
                    )),
                    Map.entry(Set.of("970.001"), Map.of(
                            "mandatory", Set.of("IFE", "CPT", "DDM", "LST"),
                            "optional", Set.of("ERR", "DI")
                    )),
                    Map.entry(Set.of("100.000"), Map.of()),
                    Map.entry(Set.of("110.000"), Map.of()),
                    Map.entry(Set.of("940.000"), Map.of()),
                    Map.entry(Set.of("081.006"), Map.of()),
                    Map.entry(Set.of("UNK"), Map.of(
                            "optional", Set.of("DEV", "DFE", "LST", "IRC", "LWC", "PM#", "DI", "ERR", "DDM",
                                    "KIT", "DA", "IND", "CPT", "IFE", "IDE")
                    ))
            )),
            Map.entry("RED", Map.ofEntries(
                    Map.entry(Set.of("081.001", "081.002", "140.000"), Map.of(
                            "mandatory", Set.of("DEV", "DFE", "LST"),
                            "optional", Set.of("IRC", "LWC", "PM#", "DI", "ERR", "RA1", "RA2",
                                    "RA3", "RA4", "RA5", "RA6", "RA7", "RB1", "RB2", "RC1", "RC2", "RD1",
                                    "RD2", "RD3", "ACC", "ANC", "MDL", "IFE", "CCM")
                    )),
                    Map.entry(Set.of("081.003"), Map.of(
                            "mandatory", Set.of("DDM", "DFE", "KIT", "LST"),
                            "optional", Set.of("IRC", "LWC", "PM#", "DI", "ERR", "RA1", "RA2",
                                    "RA3", "RA4", "RA5", "RA6", "RA7", "RB1", "RB2", "RC1", "RC2",
                                    "RD1", "RD2", "RD3", "ACC", "ANC", "MDL", "IFE", "CCM")
                    )),
                    Map.entry(Set.of("081.004"), Map.of(
                            "mandatory", Set.of("DEV", "DFE", "KIT", "LST"),
                            "optional", Set.of("IRC", "LWC", "PM#", "DI", "ERR", "RA1", "RA2",
                                    "RA3", "RA4", "RA5", "RA6", "RA7", "RB1", "RB2", "RC1", "RC2",
                                    "RD1", "RD2", "RD3", "ACC", "ANC", "MDL", "IFE", "CCM")
                    )),
                    Map.entry(Set.of("081.005"), Map.of(
                            "optional", Set.of("DA", "IND", "DEV", "DFE", "LST", "ERR", "DI", "RA1",
                                    "RA2", "RA3", "RA4", "RA5", "RA6", "RA7", "RB1", "RB2", "RC1", "RC2",
                                    "RD1", "RD2", "RD3", "ACC", "ANC", "MDL", "IFE", "CCM")
                    )),
                    Map.entry(Set.of("081.007"), Map.of(
                            "mandatory", Set.of("CPT"),
                            "optional", Set.of("LST", "PM#", "ERR", "DI", "RA1", "RA2", "RA3", "RA4",
                                    "RA5", "RA6", "RA7", "RB1", "RB2", "RC1", "RC2", "RD1", "RD2", "RD3", "ACC", "ANC", "MDL", "IFE", "CCM")
                    )),
                    Map.entry(Set.of("081.008"), Map.of(
                            "mandatory", Set.of("CPT"),
                            "optional", Set.of("DA", "IND", "ERR", "DI", "RA1", "RA2", "RA3",
                                    "RA4", "RA5", "RA6", "RA7", "RB1", "RB2", "RC1", "RC2", "RD1",
                                    "RD2", "RD3", "ACC", "ANC", "MDL", "IFE", "CCM")
                    )),
                    Map.entry(Set.of("170.000"), Map.of(
                            "mandatory", Set.of("IFE"),
                            "optional", Set.of("DFE", "LST", "IRC", "LWC", "PM#", "DDM", "DI",
                                    "ERR", "RA1", "RA2", "RA3", "RA4", "RA5", "RA6", "RA7", "RB1", "RB2",
                                    "RC1", "RC2", "RD1", "RD2", "RD3", "ACC", "ANC", "MDL", "IFE", "CCM")
                    )),
                    Map.entry(Set.of("180.010"), Map.of()),
                    Map.entry(Set.of("180.014"), Map.of()),
                    Map.entry(Set.of("180.015"), Map.of(
                            "mandatory", Set.of("IDE"),
                            "optional", Set.of("ERR", "DI", "RA1", "RA2", "RA3", "RA4", "RA5",
                                    "RA6", "RA7", "RB1", "RB2", "RC1", "RC2", "RD1", "RD2", "RD3", "ACC", "ANC", "MDL", "IFE", "CCM")
                    )),
                    Map.entry(Set.of("920.001", "950.001"), Map.of(
                            "mandatory", Set.of("DDM", "LST"),
                            "optional", Set.of("DFE", "IRC", "LWC", "PM#", "ERR", "DI", "RA1", "RA2",
                                    "RA3", "RA4", "RA5", "RA6", "RA7", "RB1", "RB2", "RC1", "RC2", "RD1",
                                    "RD2", "RD3", "ACC", "ANC", "MDL", "IFE", "CCM")
                    )),
                    Map.entry(Set.of("920.002"), Map.of(
                            "mandatory", Set.of("DFE", "LST", "DDM"),
                            "optional", Set.of("IRC", "LWC", "PM#", "ERR", "DI", "RA1", "RA2",
                                    "RA3", "RA4", "RA5", "RA6", "RA7", "RB1", "RB2", "RC1", "RC2", "RD1", "RD2",
                                    "RD3", "ACC", "ANC", "MDL", "IFE", "CCM")
                    )),
                    Map.entry(Set.of("950.002"), Map.of(
                            "optional", Set.of("DDM", "DFE", "IRC", "LST", "PM#", "LWC", "ERR", "DI",
                                    "RA1", "RA2", "RA3", "RA4", "RA5", "RA6", "RA7", "RB1", "RB2", "RC1", "RC2",
                                    "RD1", "RD2", "RD3", "ACC", "ANC", "MDL", "IFE", "CCM")
                    )),
                    Map.entry(Set.of("970.000"), Map.of(
                            "mandatory", Set.of("DEV", "DFE", "IFE", "LST"),
                            "optional", Set.of("ERR", "DI", "RA1", "RA2", "RA3", "RA4", "RA5", "RA6", "RA7", "RB1",
                                    "RB2", "RC1", "RC2", "RD1", "RD2", "RD3", "ACC", "ANC", "MDL", "IFE", "CCM")
                    )),
                    Map.entry(Set.of("970.001"), Map.of(
                            "mandatory", Set.of("IFE", "CPT", "DDM", "LST"),
                            "optional", Set.of("ERR", "DI", "RA1", "RA2", "RA3", "RA4", "RA5", "RA6", "RA7",
                                    "RB1", "RB2", "RC1", "RC2", "RD1", "RD2", "RD3", "ACC", "ANC", "MDL", "IFE", "CCM")
                    )),
                    Map.entry(Set.of("100.000"), Map.of()),
                    Map.entry(Set.of("110.000"), Map.of()),
                    Map.entry(Set.of("940.000"), Map.of()),
                    Map.entry(Set.of("081.006"), Map.of()),
                    Map.entry(Set.of("UNK"), Map.of(
                            "optional", Set.of("DEV", "DFE", "LST", "IRC", "LWC", "PM#", "DI", "ERR",
                                    "DDM", "KIT", "DA", "IND", "CPT", "IDE", "RA1", "RA2", "RA3", "RA4",
                                    "RA5", "RA6", "RA7", "RB1", "RB2", "RC1", "RC2", "RD1", "RD2", "RD3", "ACC", "ANC", "MDL", "IFE", "CCM")
                    ))
            ))
    );
    private static final Map<String, Map<String, Set<String>>> EXCLUDED_VALUES = Map.of(
            "081.001", Map.of("MF", Set.of("US")),
            "081.002", Map.of("MF", Set.of("US")),
            "081.004", Map.of("MF", Set.of("US"))
    );
    private static final Set<String> TBN_REQUIRED_PROCESSING_CODES = Set.of("RED");
    private static final Map<String, String> VALID_AOCQ_SYNTAX = Map.ofEntries(
            Map.entry("PM# ", "^(?:(?:P|D|H|K|DEN|B[DKHMR])\\d{6}|N\\d{4,6}|BP\\d{4,6})$"),
            Map.entry("DDM", "^\\d{1,10}$"),
            Map.entry("DFE", "^\\d{1,10}$"),
            Map.entry("DEV", "^\\d{1,10}$"),
            Map.entry("DI", "^.{6,23}$"),
            Map.entry("IDE", "^(?!0{4,5}$)(?:(?!G000000$)G\\d{6}|\\d{4,5}|NSR)$"),
            Map.entry("LST", "^[ABCDELQR]\\d{6}$"),
            Map.entry("DA", "^(?:BA\\d{4,6}|BN\\d{5,6}|\\d{6})$"),
            Map.entry("IND", "^\\d{4,6}$"),
            Map.entry("ACC", "^(.{7}|.{11})$"),
            Map.entry("ANC", "^(.{7}|.{11})$")
    );
    private static final List<Integer> INDUSTRY_CODE_RANGE = List.of(73,92);
    private static final Map<String,Map<String,Set<String>>> AOC_CODE_DEPENDENCIES = Map.of("RED", Map.of(
            "ACC", Set.of("RB1"),
            "ANC", Set.of("RB1"),
            "RB1", Set.of("ACC", "ANC")
    ));


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
        return MANDATORY_PARTY_TYPES.contains(partyType.toUpperCase()) ||
                OPTIONAL_PARTY_TYPES.contains(partyType.toUpperCase());
    }

    @Override
    public boolean isValueExcluded(String intendedUseCode, String partyType, String countryCode) {
        Map<String, Set<String>> partyTypeMap = EXCLUDED_VALUES.get(intendedUseCode);
        if (partyTypeMap != null) {
            Set<String> excludedCountries = partyTypeMap.get(partyType);
            return excludedCountries != null && excludedCountries.contains(countryCode);
        }
        return false;
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
        return TBN_REQUIRED_PROCESSING_CODES.contains(processingCode.toUpperCase());
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
    public Set<String> getMandatoryPartyTypes() {
        return MANDATORY_PARTY_TYPES;
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
        Map<Set<String>, Map<String, Set<String>>> intendedUseCodeLevel = SCENARIO_BASED_AOC_CODES.get(processingCode.toUpperCase());
        if (intendedUseCodeLevel != null) {
            String upperIntendedUseCode = intendedUseCode.toUpperCase();
            for (Map.Entry<Set<String>, Map<String, Set<String>>> entry : intendedUseCodeLevel.entrySet()) {
                if (entry.getKey().contains(upperIntendedUseCode)) {
                    return entry.getValue();
                }
            }
        }
        return Collections.emptyMap();
    }
    @Override
    public Map<String,Set<String>> getAocDependencies(String processingCode){
        Map<String,Set<String>> dependencies =AOC_CODE_DEPENDENCIES.get(processingCode.toUpperCase());
        if(dependencies != null){
            return dependencies;
        }
        return Collections.emptyMap();
    }
}
