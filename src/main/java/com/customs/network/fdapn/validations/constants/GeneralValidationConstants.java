package com.customs.network.fdapn.validations.constants;

import io.micrometer.common.util.StringUtils;
import lombok.Data;

import java.util.*;

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
    private static final Map<String, Set<String>> STATE_CODES = new HashMap<>();

    private static final Set<String> VALID_UOM_CODES = Set.of(
            "AE", "AM", "AP", "AT", "BA", "BB", "BC", "BD", "BE", "BF", "BG", "BH", "BI", "BJ", "BK", "BL", "BN", "BO", "BP", "BQ",
            "BR", "BS", "BT", "BU", "BV", "BX", "BY", "BZ", "CA", "CAG", "CB", "CC", "CE", "CF", "CH", "CI", "CJ", "CK", "CL", "CO",
            "CON", "CP", "CTR", "CR", "CS", "CT", "CU", "CV", "CX", "CY", "CZ", "DJ", "DP", "DR", "EN", "FC", "FD", "FI", "FL", "FO",
            "FP", "FR", "GB", "GI", "GZ", "HG", "HR", "ING", "IZ", "JC", "JG", "JR", "JT", "JY", "KEG", "KIT", "LG", "LZ", "MB", "MC",
            "MS", "MT", "MX", "NE", "NS", "NT", "PA", "PAL", "PC", "PG", "PH", "PI", "PK", "PL", "PN", "PO", "PT", "PU", "PY", "PZ",
            "RD", "RG", "RL", "RO", "RT", "RZ", "SA", "SC", "SD", "SE", "SH", "SK", "SL", "SM", "ST", "SU", "SW", "SY", "SZ", "TB",
            "TC", "TD", "TK", "TN", "TO", "TR", "TS", "TU", "TY", "TZ", "VA", "VG", "VI", "VL", "VO", "VP", "VQ", "VR", "VY", "WB"
    );

    private static final Set<String> VALID_BASE_UOM_CODES = Set.of(
            "AU", "BAU", "BBL", "BOL", "CAP", "CAR", "CFT", "CG", "CM", "CM3", "CYD", "DOZ", "DPC", "DPR",
            "FOZ", "FT", "G", "GAL", "GR", "IN", "KG", "KM", "KM2", "KM3", "L", "LB", "LNM", "M", "M2", "M3",
            "MCG", "MG", "ML", "NO", "OZ", "PCS", "PNU", "PRS", "PTL", "QTL", "SFT", "SQI", "STN", "SUP",
            "SYD", "T", "TAB", "TON", "TOZ", "YD"
    );

    private static final Set<String> VALID_INDIVIDUAL_QUALIFIER_CODES = Set.of("FD1", "PK");
    private static final Set<String> REQUIRED_INDIVIDUAL_QUALIFIER = Set.of("FD1");
    private static final Set<String> FDA_PROGRAM_CODES = Set.of(
            "BIO", "COS", "DEV", "DRU", "FOO", "RAD", "TOB", "VME", "FDA"
    );

    private static final Set<String> MANDATORY_PARTY_TYPES = Set.of(
            "MF", "DEQ", "FD1", "DP"
    );
    private static final Set<String> REPEATABLE_PARTY_TYPES = Set.of();
    private static final Set<String> VALID_PARTY_IDENTIFIER_TYPES = Set.of("16", "47");
    private static final Map<String, String> PARTY_IDENTIFIER_NUMBER_SYNTAX = new HashMap<>();
    private static final Set<String> VALID_DISCLAIMER = Set.of("A", "F");

    private static final Set<String> VALID_SOURCE_CODE_TYPES = Set.of(
            "CDB", "CMN", "244", "CPK", "CPR", "39", "CSH", "243", "CSL", "30",
            "267", "CST", "294", "HRV", "HCF", "HBA", "PMH", "SVH", "256", "262", "268"
    );
    private static final Set<String> MANDATORY_SOURCE_CODE = Set.of("39", "30");
    private static final Map<String, List<String>> ADDITIONAL_INFO_QUALIFIER_CODE = new HashMap<>();
    private static final Set<String> VALID_TEMPERATURE_QUALIFIER_CODE = Set.of("A", "F", "R", "D", "H", "U", "P");
    private static final String VALID_LOT_NUMBER_QUALIFIER = "1";
    private static final List<String> VALID_PACKAGING_QUALIFIER = List.of("1", "2", "3", "4", "5", "6");
    private static final String PRODUCT_CODE_STRUCTURE = "\\d{2}[a-zA-Z0-9][a-zA-Z\\-][a-zA-Z\\-][a-zA-Z0-9]{2}";
    private static final Set<String> VALID_ITEM_TYPE = Set.of("P");
    private static final Set<String> COUNTRIES_CODE_REQUIRED_ADDRESS = Set.of("US", "CA");
    private static final Set<String> VALID_CONSTITUENT_ACTIVE_INGREDIENT_QUALIFIER = Set.of("Y");
    private static final Set<String> GOVERNMENT_GEOGRAPHIC_CODE_QUALIFIER = Set.of("PR", "ISO", "MS", "US");
    private static final Map<String, Set<String>> LOCATION_CODES = Map.of(
            "PR", CANADA_STATE_CODES, "US", US_STATE_CODES, "MS", MEXICO_STATE_CODES,
            "ISO", COUNTRY_CODES
    );


    static {
        STATE_CODES.put("US", US_STATE_CODES);
        STATE_CODES.put("MX", MEXICO_STATE_CODES);
        STATE_CODES.put("CA", CANADA_STATE_CODES);

        PARTY_IDENTIFIER_NUMBER_SYNTAX.put("16", "\\d{9}");
        PARTY_IDENTIFIER_NUMBER_SYNTAX.put("47", "\\d{1,10}");

        ADDITIONAL_INFO_QUALIFIER_CODE.put("PG19", List.of("ENA", "AD1"));
        ADDITIONAL_INFO_QUALIFIER_CODE.put("PG20", List.of("AD2", "AD3", "AD4", "AD5", "ECI"));
        ADDITIONAL_INFO_QUALIFIER_CODE.put("PG21", List.of("INA", "EMA"));
        ADDITIONAL_INFO_QUALIFIER_CODE.put("PG07", List.of("TBN"));
    }

    private static final Set<String> FTZ_ENTRY_TYPES = Set.of("21");
    private static final Set<String> ARRIVAL_LOCATION_CODES_FOR_FTZ = Set.of("4");
    private static final Map<Boolean, List<String>> MANDATORY_ARRIVAL_INFORMATION = Map.of(
            true, List.of("A", "F"), false, List.of("A"));
    private static final String FTZ_ARRIVAL_INFO_CODE = "F";
    private static final Set<String> PRIVATELY_OWNED_VEHICLE_CODE_TYPES = Set.of("POV");


    public boolean isValidConstituentActiveIngredient(String constituentActiveIngredient) {
        return VALID_CONSTITUENT_ACTIVE_INGREDIENT_QUALIFIER.contains(constituentActiveIngredient.toUpperCase());
    }

    public boolean isValidCountryCode(String countryCode) {
        if (StringUtils.isBlank(countryCode))
            return false;
        return COUNTRY_CODES.contains(countryCode.toUpperCase());
    }

    public boolean isValidStateCode(String countryCode, String stateCode) {
        return STATE_CODES.get(countryCode.toUpperCase()).contains(stateCode.toUpperCase());
    }


    public static boolean isValidProgramCode(String programCode) {
        return FDA_PROGRAM_CODES.contains(programCode.toUpperCase());
    }

    public boolean isValidIndividualQualifierCode(String individualQualifierCode) {
        return VALID_INDIVIDUAL_QUALIFIER_CODES.contains(individualQualifierCode.toUpperCase());
    }

    public Set<String> getMandatoryPartyTypes() {
        return MANDATORY_PARTY_TYPES;
    }

    public boolean isValidPartyIdentifierType(String partyIdentifierType) {
        return VALID_PARTY_IDENTIFIER_TYPES.contains(partyIdentifierType.toUpperCase());
    }

    public boolean isValidItemType(String itemType) {
        return VALID_ITEM_TYPE.contains(itemType.toUpperCase());
    }

    public boolean isValidPartyIdentifierNumberSyntax(String partyIdentifierType, String partyIdentifierNumber) {
        return partyIdentifierNumber.matches(PARTY_IDENTIFIER_NUMBER_SYNTAX.get(partyIdentifierType.toUpperCase()));
    }

    public boolean isValidDisclaimer(String disclaimer) {
        return VALID_DISCLAIMER.contains(disclaimer.toUpperCase());
    }

    public boolean isValidUOMCode(String uomCode) {
        return VALID_UOM_CODES.contains(uomCode.toUpperCase());
    }

    public boolean isValidSourceCodeType(String sourceCodeType) {
        return VALID_SOURCE_CODE_TYPES.contains(sourceCodeType.toUpperCase());
    }

    public Set<String> getMandatorySourceCode() {
        return MANDATORY_SOURCE_CODE;
    }

    public boolean isValidAdditionalInfoQualifierCode(String pgSegment, String additionalInfoQualifierCode) {
        return ADDITIONAL_INFO_QUALIFIER_CODE.get(pgSegment.toUpperCase()).contains(additionalInfoQualifierCode.toUpperCase());
    }

    public List<String> getAdditionalInfoQualifierCode(String pgSegment) {
        return ADDITIONAL_INFO_QUALIFIER_CODE.getOrDefault(pgSegment.toUpperCase(), new ArrayList<>());
    }

    public boolean isStateAndPostalCodeRequired(String countryCode) {
        return COUNTRIES_CODE_REQUIRED_ADDRESS.contains(countryCode.toUpperCase());
    }

    public Set<String> getRequiredIndividualQualifier() {
        return REQUIRED_INDIVIDUAL_QUALIFIER;
    }

    public boolean isValidTemperatureQualifierCode(String temperatureQualifierCode) {
        return VALID_TEMPERATURE_QUALIFIER_CODE.contains(temperatureQualifierCode.toUpperCase());
    }

    public boolean isValidLotNumberQualifier(String lotNumberQualifier) {
        return VALID_LOT_NUMBER_QUALIFIER.equalsIgnoreCase(lotNumberQualifier);
    }

    public boolean isValidDegreeType(String degreeType) {
        return true;
    }

    public boolean isValidLocationOfTemperatureRecording(String locationOfTemperatureRecording) {
        return true;
    }

    public boolean isValidTemperatureIndicator(String temperatureIndicator) {
        return true;
    }

    public boolean isLotNumberRequired(String processingCode) {
        return false;
    }

    public boolean isValueExcluded(String key1, String key2, String value) {
        return false;
    }

    public Map<String, Set<String>> getAocDependencies() {
        return Collections.emptyMap();
    }

    public Map<String, Set<String>> getAocDependencies(String processingCode) {
        return Collections.emptyMap();
    }


    public boolean isRequiredIntendedUseCode(String processingCode) {
        return true;
    }
    public  boolean isRepeatableAoc(String aoc){
        return false;
    }


    public boolean isValidBaseUom(String baseUom) {
        if (StringUtils.isNotBlank(baseUom))
            return VALID_BASE_UOM_CODES.contains(baseUom.toUpperCase());
        return false;
    }

    public boolean isPartyTypeRepeatable(String partyType) {
        // generally party types are not repeatable
        return REPEATABLE_PARTY_TYPES.contains(partyType.toUpperCase());
    }
   public boolean isValidLotNumberQualifier(String lotNumberQualifier,String processingCode){
       return this.isValidLotNumberQualifier(lotNumberQualifier);
    }

    public String getValidLotNumberQualifier() {
        return VALID_LOT_NUMBER_QUALIFIER;
    }

    public List<String> getValidPackagingQualifierCode() {
        return VALID_PACKAGING_QUALIFIER;
    }

    public String getProductCodeStructure() {
        return PRODUCT_CODE_STRUCTURE;
    }

    public boolean isForeignTradeZoneEntry(String entryType) {
        return FTZ_ENTRY_TYPES.contains(entryType.toUpperCase());
    }

    public boolean isValidInspectionOrArrivalLocationCodeForFtz(String inspectionOrArrivalLocationCode) {
        return ARRIVAL_LOCATION_CODES_FOR_FTZ.contains(inspectionOrArrivalLocationCode.toUpperCase());
    }

    public String getFtzArrivalInformation() {
        return FTZ_ARRIVAL_INFO_CODE;
    }

    public List<String> getMandatoryAnticipatedArrivalInformation(boolean isFTZEntry) {
        return MANDATORY_ARRIVAL_INFORMATION.get(isFTZEntry);
    }

    public Set<String> getMandatorySourceCode(String processingCode) {
        return MANDATORY_SOURCE_CODE;
    }

    public boolean isValidGovernmentGeographicCodeQualifier(String code) {
        return GOVERNMENT_GEOGRAPHIC_CODE_QUALIFIER.contains(code.toUpperCase());
    }

    public boolean isValidLocationCode(String location, String qualifier) {
        if (LOCATION_CODES.containsKey(qualifier.toUpperCase())) {
            return LOCATION_CODES.get(qualifier.toUpperCase()).contains(location.toUpperCase());
        }
        return false;
    }

    public boolean isPrivatelyOwnedVehicle(String code) {
        return PRIVATELY_OWNED_VEHICLE_CODE_TYPES.contains(code.toUpperCase());
    }

}
