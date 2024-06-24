package com.customs.network.fdapn.validations;

import java.util.HashMap;
import java.util.Map;

public class DataViolationMessages {
    private DataViolationMessages() {
    }

    private static final Map<String, String> AOCQ_SYNTAX_ERROR_MESSAGES = new HashMap<>();
    private static final Map<String, String> PARTY_IDENTIFIER_NUMBER_ERROR_MESSAGES = new HashMap<>();

    static {

        AOCQ_SYNTAX_ERROR_MESSAGES.put("DA", "Syntax Error For AOC DA: Expected BA followed by 4-6 digits, or BN followed by 5-6 digits, or a 6-digit number.");
        AOCQ_SYNTAX_ERROR_MESSAGES.put("HRN", "Syntax Error For AOC HRN: Expected a 10-digit number for Biologic Human Cells, Tissues/ Cellular and Tissue-Based Product Establishment Registration Number (HCT/P Registration Numbers).");
        AOCQ_SYNTAX_ERROR_MESSAGES.put("IND", "Syntax Error For AOC IND: Expected 4-6 digits for Biologic Investigation New Drug Application Number (no leading zeros for CBER IND).");
        AOCQ_SYNTAX_ERROR_MESSAGES.put("BLN", "Syntax Error For AOC BLN: Expected 4-digit Biologic License Number.");
        AOCQ_SYNTAX_ERROR_MESSAGES.put("STN", "Syntax Error For AOC STN: Expected 6-digit Biologic Submission Tracking Number.");
        AOCQ_SYNTAX_ERROR_MESSAGES.put("REG", "Syntax Error For AOC REG: Expected 4-10 digits for Drug Registration Number.");
        AOCQ_SYNTAX_ERROR_MESSAGES.put("DLS", "Syntax Error For AOC DLS: Expected 10-digit Drug Listing Number.");
        AOCQ_SYNTAX_ERROR_MESSAGES.put("COS", "Invalid number format. The number must be either 7 digits or 10 digits long.");
        PARTY_IDENTIFIER_NUMBER_ERROR_MESSAGES.put("16", "Expected Exact 9-digit Party Identifier Number for partyIdentifier type 16");
        PARTY_IDENTIFIER_NUMBER_ERROR_MESSAGES.put("47", "Expected 1-10-digit Party Identifier Number for partyIdentifier type 47");

    }

    public static String getAOCQSyntaxErrorMessage(String aoc) {
        return AOCQ_SYNTAX_ERROR_MESSAGES.get(aoc);
    }

    public static String getPartyIdentifierNumberErrorMessage(String partyIdentifierType) {
        return PARTY_IDENTIFIER_NUMBER_ERROR_MESSAGES.get(partyIdentifierType);
    }
}
