package com.customs.network.fdapn.validations.constants;

public interface DeclarationRules {
    boolean isValidActionCode(String actionCode);
    boolean isValidFilingType(String filingType);
    boolean isValidReferenceQualifierCode(String referenceQualifierCode);
    boolean isValidEntryType(String entryType);
}
