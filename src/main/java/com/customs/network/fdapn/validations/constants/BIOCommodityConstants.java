package com.customs.network.fdapn.validations.constants;

import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class BIOCommodityConstants extends GeneralValidationConstants implements ConditionalValidator {
    private static final Set<String> BIOLOGICAL_AOC_CODES = Set.of(
            "BLN", "CPT", "DA", "DLS", "ERR", "HCT", "HRN", "IFE", "IND", "REG", "STN"
    );
    private static final Set<String> BIO_PROCESSING_CODES = Set.of("ALG", "VAC", "HCT", "XEN", "CGT", "BLO", "BLD", "BDP", "BBA", "PVE");


    private static final Map<String, List<Set<String>>> VALID_PARTY_TYPES = new HashMap<>();
    private static final Map<String, String> AOCQ_SYNTAX = new HashMap<>();
    private static Set<String> OPTIONAL_PARTY_TYPE=Set.of("PK");
    private static final Set<String> VALID_INTENDED_USE_CODES = Set.of(
            "080.000", "082.000", "970.000", "180.016", "150.007", "155.000", "100.000",
            "140.000", "180.009", "180.000", "110.000", "170.000", "940.000", "920.000", "UNK"
    );

    static {
        AOCQ_SYNTAX.put("DA", "^(BA\\d{4,6}|BN\\d{5,6}|\\d{6})$");
        AOCQ_SYNTAX.put("HRN", "^\\d{10}$");
        AOCQ_SYNTAX.put("IND", "^[1-9]\\d{3,5}$");
        AOCQ_SYNTAX.put("BLN", "^\\d{4}$");
        AOCQ_SYNTAX.put("STN", "^\\d{6}$");
        AOCQ_SYNTAX.put("REG", "^\\d{4,10}$");
        AOCQ_SYNTAX.put("DLS", "^\\d{10}$");
        AOCQ_SYNTAX.put("COS", "^\\d{7}$|^\\d{10}$");
    }

    @Override
    public boolean isValidAOCCode(String aocCode) {
        return BIOLOGICAL_AOC_CODES.contains(aocCode);
    }

    @Override
    public boolean isValidateAOCQSyntax(String aoc, String aocq) {
        if (isValidAOCCode(aoc)) {
            return AOCQ_SYNTAX.get(aoc).matches(aocq);
        }
        return false;
    }

    @Override
    public boolean isValidProcessingCode(String processingCode) {
        return BIO_PROCESSING_CODES.contains(processingCode.toUpperCase());
    }

    @Override
    public boolean isValidPartyType(String partyType) {
        return getMandatoryPartyTypes().contains(partyType) ||
                getOptionalPartyTypes().contains(partyType);
    }

    @Override
    public boolean isValidIntendedUseCode(String intendedUseCode) {
        return VALID_INTENDED_USE_CODES.contains(intendedUseCode);
    }

    @Override
    public String getAOCQSynatx(String aoc) {
        if(AOCQ_SYNTAX.containsKey(aoc))
            return AOCQ_SYNTAX.get(aoc);
        return null;
    }

    @Override
    public Set<String> getConditionalPartyTypes() {
        return new HashSet<>();
    }

    @Override
    public Set<String> getOptionalPartyTypes() {
        return OPTIONAL_PARTY_TYPE;
    }

    @Override
    public String getPartyIdentifierNumberSyntax(String partyIdentifierType) {
        return null;
    }

}
