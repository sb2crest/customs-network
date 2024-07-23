package com.customs.network.fdapn.validations.constants;

import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Component
public class TOBCommodityConstants extends GeneralValidationConstants implements ConditionalValidator {
    private static final Set<String> VALID_PROCESSING_CODES = Set.of("CSU", "FFM", "INV");
    private static final Set<String> VALID_INTENDED_USE_CODES = Set.of("180.001", "180.000", "UNK", "150.000", "155.000", "110.000", "130.000", "140.000", "130.037");
    private static final Map<String, Set<String>> CONDITIONAL_USE_CODES = Map.of(
            "INV", Set.of("180.001", "180.000", "UNK")
    );
    private static final Set<String> VALID_INDUSTRY_CODES = Set.of("98");
    private static final Set<String> MANDATORY_SOURCE_CODE = Set.of("39");
    private static final Set<String> TBN_REQUIRED_PROCESSING_CODES = Set.of("CSU");
    private static final Set<String> OPTIONAL_PARTY_TYPE = Set.of("RD", "TB", "PK");
    private static final Map<String, Set<String>> CONDITIONAL_PARTY_TYPES = Map.of(
            "INV", Set.of("ITL", "LAB")
    );
    private static final Map<String, Map<String, Set<String>>> SCENARIO_BASED_AOC_CODES = Map.of(
            "CSU", Map.of("optional", Set.of("ILS", "HPC", "CMT", "SE", "PMT", "EXE", "ERR", "TST"))
    );
    private static final Set<String> VALID_LOT_NUMBER_QUALIFIER = Set.of("3");
    private static final Map<String, String> VALID_AOCQ_SYNTAX = Map.of(
            "TST","^.{7,24}$"
    );
    private static final Set<String> VALID_UNIT_OF_MEASURES =Set.of("AT","BL","BN","BX","CON","CS","CT","CTR","DR","KIT","PK","VI","VL");
    private static final Set<String> VALID_BASE_UOM = Set.of("BBL","DOZ","DPC","FOZ","GAL","L","ML","NO","PCS","PTL","QTL","G","LB","KG");

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
                getOptionalPartyTypes().contains(partyType.toUpperCase()) ||
                CONDITIONAL_PARTY_TYPES.values().stream()
                        .anyMatch(set -> set.contains(partyType.toUpperCase()));
    }

    @Override
    public boolean isValidIntendedUseCode(String intendedUseCode) {
        return VALID_INTENDED_USE_CODES.contains(intendedUseCode.toUpperCase());
    }

    @Override
    public boolean isValidIntendedUseCode(String processingCode, String intendedUseCode) {
        if (CONDITIONAL_USE_CODES.containsKey(processingCode.toUpperCase())) {
            return CONDITIONAL_USE_CODES.get(processingCode.toUpperCase()).contains(intendedUseCode.toUpperCase());
        }
        return true;
    }

    @Override
    public boolean isValidProductCodeStructure(String productCode, String processingCode) {
        return false;
    }

    @Override
    public boolean isValidProductCodeStructure(String productCode) {
        return productCode.toUpperCase().matches(getProductCodeStructure()) &&
                VALID_INDUSTRY_CODES.contains(productCode.substring(0, 2));
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
    public boolean isValidLotNumberQualifier(String lotNumberQualifier) {
        return VALID_LOT_NUMBER_QUALIFIER.contains(lotNumberQualifier.toUpperCase());
    }
    @Override
    public boolean isValidUOMCode(String uomCode) {
        return VALID_UNIT_OF_MEASURES.contains(uomCode.toUpperCase());
    }

    @Override
    public boolean isValidBaseUom(String baseUom) {
        if (StringUtils.isNotBlank(baseUom))
            return VALID_BASE_UOM.contains(baseUom.toUpperCase());
        return false;
    }


    @Override
    public Set<String> getMandatorySourceCode() {
        return MANDATORY_SOURCE_CODE;
    }

    @Override
    public String getAOCQSynatx(String aoc) {
        if(VALID_AOCQ_SYNTAX.containsKey(aoc.toUpperCase())){
            return VALID_AOCQ_SYNTAX.get(aoc.toUpperCase());
        }
        return null;
    }

    @Override
    public Set<String> getConditionalPartyTypes(String processingCode) {
        if (CONDITIONAL_PARTY_TYPES.containsKey(processingCode.toUpperCase())) {
            return CONDITIONAL_PARTY_TYPES.get(processingCode.toUpperCase());
        }
        return Collections.emptySet();
    }

    @Override
    public Set<String> getOptionalPartyTypes() {
        return OPTIONAL_PARTY_TYPE;
    }

    @Override
    public String getPartyIdentifierNumberSyntax(String partyIdentifierType) {
        return null;
    }

    @Override
    public Map<String, Set<String>> getScenarioBasedAocCode(String processingCode, String intendedUseCode) {
        Map<String, Set<String>> aoc = SCENARIO_BASED_AOC_CODES.get(processingCode);
        if (aoc != null) {
            return aoc;
        }
        return Collections.emptyMap();
    }
}
