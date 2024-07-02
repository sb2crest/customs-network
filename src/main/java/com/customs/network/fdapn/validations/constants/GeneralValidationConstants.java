package com.customs.network.fdapn.validations.constants;

import io.micrometer.common.util.StringUtils;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
public class GeneralValidationConstants {
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
    private  static final Map<String,Set<String>> STATE_CODES = new HashMap<>();

    private static final Set<String> VALID_INDIVIDUAL_QUALIFIER_CODES=Set.of("FD1","PK");
    private static final Set<String> FDA_PROGRAM_CODES=Set.of(
            "BIO","COS","DEV","DRU","FOO","RAD","TOB","VME","FDA"
    );

    private static final Set<String> MANDATORY_PARTY_TYPES=Set.of(
            "MF","DEQ","FD1","DP"
    );
    private static final Set<String> VALID_PARTY_IDENTIFIER_TYPES = Set.of("16", "47");
    private static final Map<String, String> PARTY_IDENTIFIER_NUMBER_SYNTAX = new HashMap<>();


    static {
        STATE_CODES.put("US", US_STATE_CODES);
        STATE_CODES.put("MX", MEXICO_STATE_CODES);
        STATE_CODES.put("CA", CANADA_STATE_CODES);

        PARTY_IDENTIFIER_NUMBER_SYNTAX.put("16", "\\d{9}");
        PARTY_IDENTIFIER_NUMBER_SYNTAX.put("47", "\\d{1,10}");
    }

    public  boolean isValidCountryCode(String countryCode) {
        if(StringUtils.isBlank(countryCode))
            return false;
        return COUNTRY_CODES.contains(countryCode.toUpperCase());
    }
    public  boolean isValidStateCode(String countryCode,String stateCode) {
        return STATE_CODES.get(countryCode.toUpperCase()).contains(stateCode.toUpperCase());
    }
    public boolean isValidProgramCode(String programCode){
        return FDA_PROGRAM_CODES.contains(programCode.toUpperCase());
    }
    public boolean isValidIndividualQualifierCode(String individualQualifierCode){
        return VALID_INDIVIDUAL_QUALIFIER_CODES.contains(individualQualifierCode.toUpperCase());
    }
    public Set<String> getMandatoryPartyTypes(){
        return MANDATORY_PARTY_TYPES;
    }

    public boolean isValidPartyIdentifierType(String partyIdentifierType){
        return VALID_PARTY_IDENTIFIER_TYPES.contains(partyIdentifierType);
    }

    public boolean isValidPartyIdentifierNumberSyntax(String partyIdentifierType, String partyIdentifierNumber){
        return partyIdentifierNumber.matches(PARTY_IDENTIFIER_NUMBER_SYNTAX.get(partyIdentifierType));
    }
}
