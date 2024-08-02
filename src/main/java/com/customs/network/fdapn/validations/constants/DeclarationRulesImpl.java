package com.customs.network.fdapn.validations.constants;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class DeclarationRulesImpl implements  DeclarationRules{
    private static final Set<String> ACTION_CODES = Set.of("A","R","D");
    private static final Set<String> FILING_TYPES = Set.of("A","B","C","D");
    private static final Set<String> REFERENCE_QUALIFIER_CODES = Set.of("ENT","BOL","AWB","FTZ","INB");
    private static final Set<String> ENTRY_TYPES = Set.of("01","02","03","07","11","12","21","23","52","61","62","81");

    @Override
    public boolean isValidActionCode(String actionCode) {
        return ACTION_CODES.contains(actionCode.toUpperCase());
    }
    @Override
    public boolean isValidFilingType(String filingType) {
        return FILING_TYPES.contains(filingType.toUpperCase());
    }

    @Override
    public boolean isValidReferenceQualifierCode(String referenceQualifierCode) {
        return REFERENCE_QUALIFIER_CODES.contains(referenceQualifierCode.toUpperCase());
    }

    @Override
    public boolean isValidEntryType(String entryType) {
        return ENTRY_TYPES.contains(entryType.toUpperCase());
    }

}
