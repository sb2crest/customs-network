package com.customs.network.fdapn.validations;

import com.customs.network.fdapn.dto.ExcelTransactionInfo;
import com.customs.network.fdapn.dto.PriorNoticeData;
import com.customs.network.fdapn.dto.ExcelValidationResponse;
import com.customs.network.fdapn.dto.productdto.Product;
import com.customs.network.fdapn.model.ValidationError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ValidateProduct {
    private final ObjectMapper objectMapper;
    private static final String REGEX_PATTERN_FOR_ALPHANUMERIC = "^[a-zA-Z0-9]{%d}$";

    private static final String REGEX_PATTERN_FOR_ALPHANUMERIC_WITH_RANGE = "^[a-zA-Z0-9]{%d,%d}$";

    private static final String REGEX_PATTERN_FOR_ALPHA = "^[a-zA-Z]{%d}$";

    private static final String DATE_FORMAT_REGEX = "^(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[0-2])-(\\d{4})$";

    private static final String REGEX_PATTERN_FOR_ALL = "^.{%d}$";

    private static final String REGEX_PATTERN_FOR_ALL_WITH_RANGE = "^.{%d,%d}$";
    private static final String fillerPattern = "^\\s{4}$";

    private static final Set<String> Mode_Of_Transportation_Codes = Set.of("10", "11", "12", "20", "21", "30", "31", "32", "33", "34", "40", "41", "50", "60", "70");

    private static final Set<String> ENTRY_CODES = Set.of("01", "02", "03", "07", "11", "12", "21", "23", "52", "61", "62", "81");

    private static final Set<String> CARRIER_CODES = Set.of("SCAC", "IATA");

    private static final Set<String> Bill_Type_Indicator = Set.of("R", "M", "T", "H", "S", "I");

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
    private static final Map<String, List<String>> governmentAgencyProcessingCode = new HashMap<>();
    private static final Map<String, List<String>> productNumber = new HashMap<>();
    private static final Map<String, List<String>> countryOfProduction = new HashMap<>();
    private static final Map<String, List<String>> uom = new HashMap<>();
    private static final Map<String, List<String>> baseUom = new HashMap<>();

    static {
        governmentAgencyProcessingCode.put("BIO", Arrays.asList("ALG", "VAC", "HCT", "XEN", "CGT", "BLO", "BLD", "BDP", "BBA", "PVE"));
        governmentAgencyProcessingCode.put("COS", Collections.emptyList());
        governmentAgencyProcessingCode.put("DRU", Arrays.asList("PRE", "OTC", "INV", "PHN", "RND"));
        governmentAgencyProcessingCode.put("DEV", Arrays.asList("RED", "NED"));
        governmentAgencyProcessingCode.put("RAD", List.of("REP"));
        governmentAgencyProcessingCode.put("TOB", Arrays.asList("CSU", "FFM", "INV"));
        governmentAgencyProcessingCode.put("VME", Arrays.asList("ADE", "ADR"));

        productNumber.put("BIO", List.of("57"));
        productNumber.put("COS", Arrays.asList("50", "53"));
        productNumber.put("DRU", Arrays.asList("54", "56", "60", "61", "62", "63", "64", "65", "66"));
        productNumber.put("DEV", Arrays.asList("73", "74", "75", "76", "77", "78", "79", "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "90", "91", "92"));
        productNumber.put("RAD", Arrays.asList("94", "95", "96", "97"));
        productNumber.put("TOB", List.of("98"));
        productNumber.put("VME", Arrays.asList("54", "56", "60", "61", "62", "63", "64", "65", "66", "67"));

        countryOfProduction.put("BIO", Arrays.asList("39", "30"));
        countryOfProduction.put("COS", List.of("39"));
        countryOfProduction.put("DRU", Arrays.asList("30", "39"));
        countryOfProduction.put("DEV", Arrays.asList("30", "39"));
        countryOfProduction.put("RAD", Arrays.asList("30", "39"));
        countryOfProduction.put("TOB", Arrays.asList("39", "262", "HRV", "30"));
        countryOfProduction.put("VME", Arrays.asList("30", "39"));

        uom.put("BIO", Arrays.asList("AE", "AM", "AP", "AT", "BA", "BC", "BG", "BO", "BQ", "BS", "BV", "BX", "CA", "CI", "CON", "CS", "CT", "CX", "CY", "DR", "EN", "FD", "GB", "MB", "PAL", "PC", "PK", "SY", "VI", "TU", "VP", "VL"));
        uom.put("COS", List.of());
        uom.put("DRU", List.of());
        uom.put("DEV", Arrays.asList("CS", "CT", "BX", "PK"));
        uom.put("RAD", Arrays.asList("CS", "CT", "BX", "PK"));
        uom.put("TOB", Arrays.asList("AT", "BL", "BN", "BX", "CON", "CS", "CT", "CTR", "DR", "KIT", "PK", "VI", "VL"));
        uom.put("VME", List.of());

        baseUom.put("BIO", Arrays.asList("AU", "BAU", "CAP", "CG", "FOZ", "G", "GAL", "KG", "L", "LB", "MG", "ML", "MCG", "NO", "OZ", "PCS", "PNU", "PTL", "QTL", "TAB"));
        baseUom.put("COS", List.of());
        baseUom.put("DRU", Arrays.asList("BBL", "BOL", "CAP", "CFT", "CG", "CM", "CM3", "CYD", "FOZ", "FT", "G", "GAL", "KG", "KM", "KM2", "KM3", "L", "LB", "LNM", "M", "M2", "M3", "MG", "MCG", "ML", "OZ", "PCS", "PTL", "QTL", "STN", "SUP", "T", "TAB", "TON", "TOZ"));
        baseUom.put("DEV", List.of("PCS"));
        baseUom.put("RAD", List.of("PCS"));
        baseUom.put("TOB", Arrays.asList("BBL", "DOZ", "DPC", "FOZ", "GAL", "L", "ML", "NO", "PCS", "PTL", "QTL", "G", "KG", "LB"));
        baseUom.put("VME", List.of());

    }

    public ValidateProduct(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<ValidationError> validateProduct(JsonNode productInfo) throws JsonProcessingException {
        List<ValidationError> validationErrorList = new ArrayList<>();
        Product product = objectMapper.treeToValue(productInfo, Product.class);
        //do validation
        return validationErrorList;
    }
    public List<ExcelValidationResponse> validateExcelTransactions(List<ExcelTransactionInfo> transactions){
      return transactions.stream()
                .filter(Objects::nonNull)
                .map(obj->{
                    ExcelValidationResponse response = new ExcelValidationResponse();
                    response.setExcelTransactionInfo(obj);

                    List<ValidationError> validationErrorList = new ArrayList<>();
                    PriorNoticeData data=obj.getPriorNoticeData();
                    //do validation
                    response.setValidationErrorList(validationErrorList);

                    return response;

                }).toList();
    }

    private boolean isValidDateFormat(String dateStr) {
        Pattern pattern = Pattern.compile(DATE_FORMAT_REGEX);
        Matcher matcher = pattern.matcher(dateStr);
        return matcher.matches();
    }

    private ValidationError createValidationError(String fieldName, String message, Object actual) {
        ValidationError validationError = new ValidationError();
        validationError.setFieldName(fieldName);
        validationError.setMessage(message);
        validationError.setActual(actual);
        return validationError;
    }

    private static boolean isValidProcessingCode(String processingCode) {
        for (List<String> codes : governmentAgencyProcessingCode.values()) {
            if (codes.contains(processingCode.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidProductCode(String productCode) {
        for (List<String> codes : productNumber.values()) {
            if (codes.contains(productCode)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidCountryOfProductionCode(String copCode) {
        for (List<String> codes : countryOfProduction.values()) {
            if (codes.contains(copCode)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidUomCode(String uomCode) {
        for (List<String> codes : uom.values()) {
            if (codes.contains(uomCode.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isValidBaseUomCode(String baseUomCode) {
        for (List<String> codes : baseUom.values()) {
            if (codes.contains(baseUomCode.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
    private boolean isValidArrivalTime(String time) {
        String pattern = "^(?:(0[0-9]|1[0-9]|2[0-3]):[0-5][0-9])|(?:(0[0-9]|1[0-9]|2[0-3])[0-5][0-9])|(?:(0[1-9]|1[0-2]):[0-5][0-9]\\s?(AM|PM|am|pm))$";

        if (!time.matches(pattern)) {
            return false;
        }
        if (time.matches("^(0[1-9]|1[0-2]):[0-5][0-9]\\s?(AM|PM|am|pm)?$")) {
            String[] parts = time.split("\\s+");
            String ampm = parts[1].toUpperCase();
            if (!ampm.equals("AM") && !ampm.equals("PM")) {
                return false;
            }
        }

        return true;
    }

}
