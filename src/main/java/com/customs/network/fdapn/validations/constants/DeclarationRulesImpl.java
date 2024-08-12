package com.customs.network.fdapn.validations.constants;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DeclarationRulesImpl implements  DeclarationRules{
    private static final Set<String> ACTION_CODES = Set.of("A","R","D");
    private static final Set<String> FILING_TYPES = Set.of("A","B","C","D");
    private static final Set<String> REFERENCE_QUALIFIER_CODES = Set.of("ENT","BOL","AWB","FTZ","INB");
    private static final Set<String> ENTRY_TYPES = Set.of("01","02","03","07","11","12","21","23","52","61","62","81");
    private static final Set<String> MODE_OF_TRANSPORT = Set.of("10", "11", "12", "20", "21", "30", "31", "32", "33", "34", "40", "41", "50", "60", "70");
    private static final Set<String> BILL_TYPE_INDICATOR_FOR_PE10 = Set.of("R","M","T");
    private static final Set<String> BILL_TYPE_INDICATOR_FOR_PE15 = Set.of("R","M","T","H","S","I");
    private static final Set<String> CARRIER_CODES = Set.of("SCAC","IATA");

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

    @Override
    public boolean isValidaModeOfTransportCode(String motCode){
        return MODE_OF_TRANSPORT.contains(motCode.toUpperCase());
    }

    @Override
    public boolean isValidBillTypeIndicatorForPE10(String billTypeIndicator){
        return BILL_TYPE_INDICATOR_FOR_PE10.contains(billTypeIndicator.toUpperCase());
    }

    @Override
    public boolean isValidBillTypeIndicatorForPE15(String billTypeIndicator){
        return BILL_TYPE_INDICATOR_FOR_PE15.contains(billTypeIndicator.toUpperCase());
    }

    @Override
    public boolean isValidCarrierCode(String carrierCode){
        return CARRIER_CODES.contains(carrierCode.toUpperCase());
    }
}
