package com.customs.network.fdapn.validations.constants;

import com.customs.network.fdapn.exception.ErrorResCodes;
import com.customs.network.fdapn.exception.FdapnCustomExceptions;
import com.customs.network.fdapn.model.CommodityValidationRules;
import com.customs.network.fdapn.repository.CommodityValidationRulesRepository;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class BIOCommodityConstants extends GeneralValidationConstants implements ConditionalValidator {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        BIOCommodityConstants that = (BIOCommodityConstants) o;
        return Objects.equals(commodityValidationRulesRepository, that.commodityValidationRulesRepository) && Objects.equals(industryCode, that.industryCode) && Objects.equals(biologicalAocCodes, that.biologicalAocCodes) && Objects.equals(bioProcessingCodes, that.bioProcessingCodes) && Objects.equals(aocqSyntax, that.aocqSyntax) && Objects.equals(optionalPartyType, that.optionalPartyType) && Objects.equals(validIntendedUseCodes, that.validIntendedUseCodes) && Objects.equals(cberRegulatedProcessingCodes, that.cberRegulatedProcessingCodes) && Objects.equals(aocUseCases, that.aocUseCases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), commodityValidationRulesRepository, industryCode, biologicalAocCodes, bioProcessingCodes, aocqSyntax, optionalPartyType, validIntendedUseCodes, cberRegulatedProcessingCodes, aocUseCases);
    }

    private final CommodityValidationRulesRepository commodityValidationRulesRepository;

    BIOCommodityConstants(CommodityValidationRulesRepository commodityValidationRulesRep) {
        this.commodityValidationRulesRepository = commodityValidationRulesRep;
    }

    private String industryCode;
    private Set<String> biologicalAocCodes;
    private Set<String> bioProcessingCodes;
    private Map<String, String> aocqSyntax;
    private Set<String> optionalPartyType;
    private Set<String> validIntendedUseCodes;
    private Set<String> cberRegulatedProcessingCodes;
    private Map<String, Map<Set<String>, Map<String, Set<String>>>> aocUseCases;



    @PostConstruct
    private void init() {
        log.info("Initializing the constants for Biological commodity...");
        long startTime = System.currentTimeMillis();
        CommodityValidationRules rules = commodityValidationRulesRepository.findById("BIO").orElse(null);
        if (rules == null) {
            log.error("Could not find any rules for Biological commodity.");
            throw new FdapnCustomExceptions(ErrorResCodes.NOT_FOUND, "BIO Commodity validation rules not found in database.");
        }
        JsonNode rulesJson = rules.getRules();

        industryCode = rulesJson.get("INDUSTRY_CODE").asText();

        biologicalAocCodes = new HashSet<>();
        rulesJson.get("BIOLOGICAL_AOC_CODES").forEach(code -> biologicalAocCodes.add(code.asText()));

        bioProcessingCodes = new HashSet<>();
        rulesJson.get("BIO_PROCESSING_CODES").forEach(code -> bioProcessingCodes.add(code.asText()));

        aocqSyntax = new HashMap<>();
        JsonNode aocqSyntaxMap = rulesJson.get("AOCQ_SYNTAX");
        aocqSyntaxMap.fields().forEachRemaining(entry -> this.aocqSyntax.put(entry.getKey(), entry.getValue().asText()));

        optionalPartyType = new HashSet<>();
        rulesJson.get("OPTIONAL_PARTY_TYPE").forEach(code -> optionalPartyType.add(code.asText()));

        validIntendedUseCodes = new HashSet<>();
        rulesJson.get("VALID_INTENDED_USE_CODES").forEach(code -> validIntendedUseCodes.add(code.asText()));

        cberRegulatedProcessingCodes = new HashSet<>();
        rulesJson.get("CBER_REGULATED_PROCESSING_CODES").forEach(code -> cberRegulatedProcessingCodes.add(code.asText()));

        aocUseCases = new HashMap<>();
        JsonNode aocUseCasesNode = rulesJson.get("AOC_USE_CASES");
        aocUseCasesNode.fields().forEachRemaining(entry -> {
            String key = entry.getKey();
            JsonNode value = entry.getValue();
            Map<Set<String>, Map<String, Set<String>>> innerMap = new HashMap<>();
            value.fields().forEachRemaining(innerEntry -> {
                Set<String> innerKey = new HashSet<>(Arrays.asList(innerEntry.getKey().split(",")));
                Map<String, Set<String>> innerValue = new HashMap<>();
                innerEntry.getValue().fields().forEachRemaining(innerInnerEntry -> {
                    Set<String> set = new HashSet<>();
                    innerInnerEntry.getValue().forEach(item -> set.add(item.asText()));
                    innerValue.put(innerInnerEntry.getKey(), set);
                });
                innerMap.put(innerKey, innerValue);
            });
            aocUseCases.put(key, innerMap);
        });
        long endTime= System.currentTimeMillis();
        log.info("Completed initialization of Biological Commodity in {} milliseconds ", endTime-startTime);
    }

    @Override
    public boolean isValidAOCCode(String aocCode) {
        return biologicalAocCodes.contains(aocCode.toUpperCase());
    }

    @Override
    public boolean isValidAOCQSyntax(String aoc, String aocq) {
        if (isValidAOCCode(aoc.toUpperCase())) {
            return aocqSyntax.get(aoc.toUpperCase()).equals(aocq.toUpperCase());
        }
        return false;
    }

    @Override
    public boolean isValidProcessingCode(String processingCode) {
        return bioProcessingCodes.contains(processingCode.toUpperCase());
    }

    @Override
    public boolean isValidPartyType(String partyType) {
        return getMandatoryPartyTypes().contains(partyType.toUpperCase()) ||
                getOptionalPartyTypes().contains(partyType.toUpperCase());
    }

    @Override
    public boolean isValidIntendedUseCode(String intendedUseCode) {
        return validIntendedUseCodes.contains(intendedUseCode.toUpperCase());
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
        return productCode.matches(getProductCodeStructure()) &&
                productCode.startsWith(industryCode.toUpperCase());
    }

    @Override
    public boolean isTBNRequired(String processingCode) {
        return cberRegulatedProcessingCodes.contains(processingCode.toUpperCase());
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
        if (aocqSyntax.containsKey(aoc.toUpperCase()))
            return aocqSyntax.get(aoc.toUpperCase());
        return null;
    }

    @Override
    public Set<String> getConditionalPartyTypes(String processingCode) {
        return new HashSet<>();
    }

    @Override
    public Set<String> getOptionalPartyTypes() {
        return optionalPartyType;
    }

    @Override
    public String getPartyIdentifierNumberSyntax(String partyIdentifierType) {
        return null;
    }

    @Override
    public Map<String, Set<String>> getScenarioBasedAocCode(String processingCode, String intendedUseCode) {
        Map<Set<String>, Map<String, Set<String>>> intendedUseCodeLevel = aocUseCases.getOrDefault(intendedUseCode.toUpperCase(), Collections.emptyMap());
        for (Map.Entry<Set<String>, Map<String, Set<String>>> entry : intendedUseCodeLevel.entrySet()) {
            Set<String> processingCodeSet = entry.getKey();
            if (processingCodeSet.contains(processingCode.toUpperCase())) {
                return entry.getValue();
            }
        }
        return Collections.emptyMap();
    }
}
