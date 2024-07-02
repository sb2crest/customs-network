package com.customs.network.fdapn.validations;

import com.customs.network.fdapn.dto.ExcelTransactionInfo;
import com.customs.network.fdapn.dto.PriorNoticeData;
import com.customs.network.fdapn.dto.ExcelValidationResponse;
import com.customs.network.fdapn.validations.productdto.Product;
import com.customs.network.fdapn.model.ValidationError;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
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
        long start = System.currentTimeMillis();
        Product product = objectMapper.treeToValue(productInfo, Product.class);
        List<ValidationError> validationErrorList = new ArrayList<>(checkInitialViolations(product));
//        validationErrorList.addAll(validateProgramCodeBeforeFurtherValidation(product));
//        long end = System.currentTimeMillis();
//        log.info("Validation for product completed in {} seconds ",(end-start)/1000);
        return new ArrayList<>();
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

    private <T> List<ValidationError> checkInitialViolations(T obj){
        List<ValidationError> validationErrorList = new ArrayList<>();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<T>> violations = validator.validate(obj);
        for (ConstraintViolation<T> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            String message = violation.getMessage();
            Object actual = violation.getInvalidValue();
            ValidationError validationError = new ValidationError();
            validationError.setFieldName(fieldName);
            validationError.setMessage(message);
            validationError.setActual(actual);
            validationErrorList.add(validationError);
        }
        return validationErrorList;
    }
    private List<ValidationError> validateProgramCodeBeforeFurtherValidation(Product product){
        String programCode = product.getGovernmentAgencyProgramCode();
        if(StringUtils.isBlank(programCode))
            return Collections.emptyList();
        List<ValidationError> validationErrorList = new ArrayList<>();
        if(ValidationConstants.isValidProgramCode(programCode)){
            //party details validation
            validationErrorList.addAll(DataValidator.validatePartyDetails(product.getPartyDetails(),programCode));
            //affirmation of compliance  validation
            validationErrorList.addAll(DataValidator.validateAffirmationOfCompliance(product.getAffirmationOfCompliance(),programCode));
        }else{
            validationErrorList.add(createValidationError("governmentAgencyProgramCode", "Invalid Program Code", programCode));
        }
        return validationErrorList;
    }
    private ValidationError createValidationError(String fieldName, String message, Object actual) {
        ValidationError validationError = new ValidationError();
        validationError.setFieldName(fieldName);
        validationError.setMessage(message);
        validationError.setActual(actual);
        return validationError;
    }

}
