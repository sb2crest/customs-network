package com.customs.network.fdapn.validations.constants;

public interface DeclarationRules {
    boolean isValidActionCode(String actionCode);
    boolean isValidFilingType(String filingType);
    boolean isValidReferenceQualifierCode(String referenceQualifierCode);
    boolean isValidEntryType(String entryType);

    boolean isValidaModeOfTransportCode(String motCode);

    boolean isValidBillTypeIndicatorForPE10(String billTypeIndicator);

    boolean isValidBillTypeIndicatorForPE15(String billTypeIndiacator);

    boolean isValidCarrierCode(String carrierCode);
}
