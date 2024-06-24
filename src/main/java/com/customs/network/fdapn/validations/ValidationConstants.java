package com.customs.network.fdapn.validations;

import io.micrometer.common.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ValidationConstants {
    private ValidationConstants(){}
    private static final Set<String> COUNTRY_CODES = Set.of(
            "AF", "AX", "AL", "DZ", "AS", "AD", "AO", "AI", "AQ", "AG", "AR", "AM", "AW", "AU", "AT", "AZ", "BS", "BH",
            "BD", "BB", "BY", "BE", "BZ", "BJ", "BM", "BT", "BO", "BA", "BW", "BV", "BR", "IO", "BN", "BG", "BF", "BI",
            "KH", "CM", "CA", "CV", "KY", "CF", "TD", "CL", "CN", "CX", "CC", "CO", "KM", "CG", "CD", "CK", "CR", "CI",
            "HR", "CU", "CY", "CZ", "DK", "DJ", "DM", "DO", "TP", "EC", "EG", "SV", "GQ", "ER", "EE", "ET", "FK", "FO",
            "FJ", "FI", "FR", "GF", "PF", "TF", "GA", "GM", "GE", "DE", "GH", "GI", "GR", "GL", "GD", "GP", "GU", "GT",
            "GG", "GN", "GW", "GY", "HT", "HM", "VA", "HN", "HK", "HU", "IS", "IN", "ID", "IR", "IQ", "IE", "IM", "IL",
            "IT", "JM", "JP", "JE", "JO", "KZ", "KE", "KI", "KP", "KR", "KW", "KG", "LA", "LV", "LB", "LS", "LR", "LY",
            "LI", "LT", "LU", "MO", "MK", "MG", "MW", "MY", "MV", "ML", "MT", "MH", "MQ", "MR", "MU", "YT", "MX", "FM",
            "MD", "MC", "MN", "ME", "MS", "MA", "MZ", "MM", "NA", "NR", "NP", "NL", "AN", "NC", "NZ", "NI", "NE", "NG",
            "NU", "NF", "MP", "NO", "OM", "PK", "PW", "PS", "PA", "PG", "PY", "PE", "PH", "PN", "PL", "PT", "PR", "QA",
            "RE", "RO", "RU", "RW", "SH", "KN", "LC", "PM", "VC", "WS", "SM", "ST", "SA", "SN", "RS", "SC", "SL", "SG",
            "SK", "SI", "SB", "SO", "ZA", "GS", "ES", "LK", "SD", "SR", "SJ", "SZ", "SE", "CH", "SY", "TW", "TJ", "TZ",
            "TH", "TL", "TG", "TK", "TO", "TT", "TN", "TR", "TM", "TC", "TV", "UG", "UA", "AE", "GB", "US", "UM", "UY",
            "UZ", "VU", "VE", "VN", "VG", "VI", "WF", "EH", "YE", "YU", "ZM", "ZW"
    );
    private static final Set<String> US_STATE_CODES = Set.of(
            "AK", "AL", "AR", "AZ", "CA", "CO", "CT", "DC", "DE", "FL", "GA", "HI", "IA", "ID", "IL", "IN", "KS",
            "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MO", "MS", "MT", "NC", "ND", "NE", "NH", "NJ", "NM", "NV",
            "NY", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VA", "VT", "WA", "WI", "WV", "WY"
    );

    private static final Set<String> MEXICO_STATE_CODES = Set.of(
            "AGU", "BCN", "BCS", "CAM", "CHH", "CHP", "COA", "COL", "DIF", "DUR", "GRO", "GUA", "HID", "JAL", "MEX",
            "MIC", "MOR", "NAY", "NLE", "OAX", "PUE", "QUE", "ROO", "SIN", "SLP", "SON", "TAB", "TAM", "TLA", "VER",
            "YUC", "ZAC"
    );

    private static final Set<String> CANADA_STATE_CODES = Set.of(
            "AB", "BC", "MB", "NB", "NL", "NS", "NT", "NU", "ON", "PE", "QC", "SK", "YT"
    );
    private static final Set<String> BIOLOGICAL_AOC_CODES=Set.of(
            "BLN","CPT","DA","DLS","ERR","HCT","HRN","IFE","IND","REG","STN"
    );
    private static final Set<String> COSMETICS_AOC_CODES=Set.of("COS","ERR","IFE");
    private static final Set<String> DRUGS_AOC_CODES=Set.of("DA","DLS","ERR","FSR","IDE","IND","LST","PLR","PM#","PRN","REG");
    private static final Set<String> TOBACCO_AOC_CODES=Set.of("CMT","ERR","EXE","HPC","ILS","PMT","SE","TST");
    private  static final Map<String,Set<String>> STATE_CODES = new HashMap<>();
    private static final Map<String,Set<String>> AOC_CODES = new HashMap<>();
    static {
        STATE_CODES.put("US", US_STATE_CODES);
        STATE_CODES.put("MX", MEXICO_STATE_CODES);
        STATE_CODES.put("CA", CANADA_STATE_CODES);
        //aoc information
        AOC_CODES.put("BIO",BIOLOGICAL_AOC_CODES);
        AOC_CODES.put("COS",COSMETICS_AOC_CODES);
        AOC_CODES.put("DRU",DRUGS_AOC_CODES);
        AOC_CODES.put("TOB",TOBACCO_AOC_CODES);
    }

    private static final Map<String, List<Set<String>>> VALID_PARTY_TYPES =new HashMap<>();
    static {
        //Valid PARTY_TYPES Mandatory/conditional/optional (OTHER THAN FOO)
        VALID_PARTY_TYPES.put("BIO",List.of(Set.of("MF","DEQ","FD1","DP"),Set.of(),Set.of("PK")));
        VALID_PARTY_TYPES.put("COS",List.of(Set.of("MF","DEQ","FD1","DP"),Set.of(),Set.of("PK")));
        VALID_PARTY_TYPES.put("DEV",List.of(Set.of("MF","DEQ","FD1","DP"),Set.of(),Set.of("PK")));
        VALID_PARTY_TYPES.put("DRU",List.of(Set.of("MF","DEQ","FD1","DP"),Set.of(),Set.of("PK","SPO","GD")));
        VALID_PARTY_TYPES.put("RAD",List.of(Set.of("MF","DEQ","FD1","DP"),Set.of(),Set.of("PK")));
        VALID_PARTY_TYPES.put("TOB",List.of(Set.of("MF","DEQ","FD1","DP"),Set.of("ITL","LAB"),Set.of("PK","RD","TB")));
        VALID_PARTY_TYPES.put("VME",List.of(Set.of("MF","DEQ","FD1","DP"),Set.of(),Set.of("GD","PK")));
    }
    private static final Set<String> VALID_INDIVIDUAL_QUALIFIER_CODES=Set.of("FD1","PK");

    private static final Map<String, String> AOCQ_SYNTAX = new HashMap<>();
    private static final Map<String, String> PARTY_IDENTIFIER_NUMBER_SYNTAX = new HashMap<>();
    private static final Set<String> VALID_PARTY_IDENTIFIER_TYPES = Set.of("16","47");
    static {
        AOCQ_SYNTAX.put("DA", "^(BA\\d{4,6}|BN\\d{5,6}|\\d{6})$");
        AOCQ_SYNTAX.put("HRN", "^\\d{10}$");
        AOCQ_SYNTAX.put("IND", "^[1-9]\\d{3,5}$");
        AOCQ_SYNTAX.put("BLN", "^\\d{4}$");
        AOCQ_SYNTAX.put("STN", "^\\d{6}$");
        AOCQ_SYNTAX.put("REG", "^\\d{4,10}$");
        AOCQ_SYNTAX.put("DLS", "^\\d{10}$");
        AOCQ_SYNTAX.put("COS","^\\d{7}$|^\\d{10}$");

        PARTY_IDENTIFIER_NUMBER_SYNTAX.put("16","\\d{9}");
        PARTY_IDENTIFIER_NUMBER_SYNTAX.put("47","\\d{1,10}");
    }
    private static final Set<String> FDA_PROGRAM_CODES=Set.of(
            "BIO","COS","DEV","DRU","FOO","RAD","TOB","VME","FDA"
    );
    private static final Map<String,Set<String>> VALID_PROCESSING_CODES = new HashMap<>();
    private static final Set<String> BIO_PROCESSING_CODES=Set.of("ALG", "VAC", "HCT", "XEN", "CGT", "BLO", "BLD", "BDP", "BBA", "PVE");
    private static final Set<String> COS_PROCESSING_CODES=Set.of();
    private static final Set<String> DRU_PROCESSING_CODES=Set.of("PRE", "OTC", "INV", "PHN", "RND");
    private static final Set<String> DEV_PROCESSING_CODES=Set.of("RED", "NED");
    private static final Set<String> RAD_PROCESSING_CODES=Set.of("REP");
    private static final Set<String> TOB_PROCESSING_CODES=Set.of("CSU", "FFM", "INV");
    private static final Set<String> VME_PROCESSING_CODES=Set.of("ADE", "ADR");
    private static final Set<String> FOO_PROCESSING_CODES=Set.of("NSF", "PRO","FEE","ADD","DSU","CCW");
    static {
        VALID_PROCESSING_CODES.put("BIO",BIO_PROCESSING_CODES);
        VALID_PROCESSING_CODES.put("COS",COS_PROCESSING_CODES);
        VALID_PROCESSING_CODES.put("DRU",DRU_PROCESSING_CODES);
        VALID_PROCESSING_CODES.put("DEV",DEV_PROCESSING_CODES);
        VALID_PROCESSING_CODES.put("RAD",RAD_PROCESSING_CODES);
        VALID_PROCESSING_CODES.put("TOB",TOB_PROCESSING_CODES);
        VALID_PROCESSING_CODES.put("VME",VME_PROCESSING_CODES);
        VALID_PROCESSING_CODES.put("FOO",FOO_PROCESSING_CODES);
    }

    public static boolean isValidProgramCode(String programCode) {
        return FDA_PROGRAM_CODES.contains(programCode.toUpperCase());
    }
    public static boolean isValidCountryCode(String countryCode) {
        if(StringUtils.isBlank(countryCode))
            return false;
        return COUNTRY_CODES.contains(countryCode.toUpperCase());
    }
    public static boolean isValidStateCode(String countryCode,String stateCode) {
        return STATE_CODES.get(countryCode.toUpperCase()).contains(stateCode.toUpperCase());
    }
    public static boolean isValidIndividualQualifierCode(String individualQualifierCode) {
        return VALID_INDIVIDUAL_QUALIFIER_CODES.contains(individualQualifierCode.toUpperCase());
    }
    public static boolean isValidAOCCode(String programCode,String aocCode) {
        return AOC_CODES.get(programCode.toUpperCase()).contains(aocCode.toUpperCase());
    }
    public static String getAOCQSynatx(String programCode,String aoc){
        boolean isValidAOCCode =AOC_CODES.get(programCode.toUpperCase()).contains(aoc.toUpperCase());
        if(isValidAOCCode)
            return AOCQ_SYNTAX.get(aoc);
        return null;
    }
    public static boolean validateAOCQSyntax(String programCode,String aoc, String aocq) {
        if (StringUtils.isNotBlank(aocq)) {
            String syntax = getAOCQSynatx(programCode,aoc);
            if (syntax != null) {
                return aocq.matches(syntax);
            }
        }
        return true;
    }
    public static boolean isValidProcessingCode(String programCode,String processingCode) {
        return VALID_PROCESSING_CODES.get(programCode.toUpperCase()).contains(processingCode.toUpperCase());
    }
    //FOO EXCLUDED
    public static Set<String> getMandatoryPartyTypes(String programCode){
        return VALID_PARTY_TYPES.get(programCode.toUpperCase()).get(0);
    }
    public static Set<String> getConditionalPartyTypes(String programCode){
        return VALID_PARTY_TYPES.get(programCode.toUpperCase()).get(1);
    }
    public static Set<String> getOptionalPartyTypes(String programCode){
        return VALID_PARTY_TYPES.get(programCode.toUpperCase()).get(2);
    }
    public static boolean isValidPartyType(String programCode,String partyType){
        return getMandatoryPartyTypes(programCode).contains(partyType.toUpperCase()) ||
                getConditionalPartyTypes(programCode).contains(partyType.toUpperCase()) ||
                getOptionalPartyTypes(programCode).contains(partyType.toUpperCase());
    }
    public static String getPartyIdentifierNumberSyntax(String partyIdentifierType){
        return PARTY_IDENTIFIER_NUMBER_SYNTAX.get(partyIdentifierType);
    }
    public static boolean isValidPartyIdentifierType(String partyIdentifierType){
        return VALID_PARTY_IDENTIFIER_TYPES.contains(partyIdentifierType);
    }
}
