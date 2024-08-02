package com.customs.network.fdapn.validations;

import java.util.HashMap;
import java.util.Map;

public class DataViolationMessages {
    private DataViolationMessages() {
    }

    private static final Map<String, Map<String, String>> AOCQ_SYNTAX_ERROR_MESSAGES = new HashMap<>();
    private static final Map<String, String> PARTY_IDENTIFIER_NUMBER_ERROR_MESSAGES = new HashMap<>();

    static {
        AOCQ_SYNTAX_ERROR_MESSAGES.put("BIO", Map.of("DA", "Syntax Error For AOC DA: Expected BA followed by 4-6 digits, or BN followed by 5-6 digits, or a 6-digit number.",
                "HRN", "Syntax Error For AOC HRN: Expected a 10-digit number for Biologic Human Cells, Tissues/ Cellular and Tissue-Based Product Establishment Registration Number (HCT/P Registration Numbers).",
                "IND", "Syntax Error For AOC IND: Expected 4-6 digits for Biologic Investigation New Drug Application Number (no leading zeros for CBER IND).",
                "BLN", "Syntax Error For AOC BLN: Expected 4-digit Biologic License Number.",
                "STN", "Syntax Error For AOC STN: Expected 6-digit Biologic Submission Tracking Number.",
                "REG", "Syntax Error For AOC REG: Expected 4-10 digits for Drug Registration Number.",
                "DLS", "Syntax Error For AOC DLS: Expected 10-digit Drug Listing Number."
        ));
        AOCQ_SYNTAX_ERROR_MESSAGES.put("COS", Map.of("COS", "Invalid number format. The number must be either 7 digits or 10 digits long."));
        AOCQ_SYNTAX_ERROR_MESSAGES.put("DRU", Map.of("DA", "Either DA New Drug Application Number or Abbreviated New Drug Application followed by exactly six numeric digits (6N).",
                "REG", "The input does not match the required format for a Drug Registration Number. Please ensure the format consists of exactly nine numeric digits.",
                "DLS", "The input does not match the required format for a Drug Listing Number. Please ensure the format consists of exactly ten numeric digits.",
                "IND", "The input does not match the required format for an Investigational New Drug Number. Please ensure the format consists of exactly six numeric digits.",
                "FSR", "The input does not match the required format for a Foreign Seller Registration Number. Please ensure the format consists of exactly nine numeric digits.",
                "PRN", "The input does not match the required format for a Pre-import Request Number. Please ensure the format consists of between 6 to 10 numeric digits",
                "LST", "The input does not match the required format for a Device Listing Number. Please ensure the format starts with one of: A, B, C, D, E, L, Q, R followed by exactly six numeric digits.",
                "PM#", "The input does not match the required format for a Device Premarket Number. Please ensure the format matches one of: P+6N, N+4N/5N/6N, D+6N, H+6N, K+6N, DEN+6N.",
                "IDE", "The input does not match the required format for an Investigational Device Exemption Number. Please ensure the format is either G+6N (G followed by six digits) or exactly 'NSR'."
        ));
        AOCQ_SYNTAX_ERROR_MESSAGES.put("RAD", Map.of(
                "RA1", " Invalid RA1 date format.",
                "ACC", "Invalid ACC Product Report accession number format. The accession number must be either 7 or 11 characters long.",
                "ANC"," Invalid ANC Annual Report accession number format. Please ensure the accession number is either 7 or 11 characters long."
                ));
        AOCQ_SYNTAX_ERROR_MESSAGES.put("TOB",Map.of(
                "TST","Please enter a valid TST Tobacco Submission Tracking Number in the format 7X or 7X - 24X."
        ));
        AOCQ_SYNTAX_ERROR_MESSAGES.put("DEV", Map.of("PM#", "Invalid PM# Device Premarket Number format",
                "DDM", "The DDM Device Domestic Manufacturer number format is invalid because it does not meet the criteria—it must be between 1 and 10 digits long and contain only numeric characters (0-9).",
                "DEV", "The DEV Device Foreign Manufacturer Registration Number must be between 1 and 10 digits long and consist solely of numeric characters (0-9). Please correct the format accordingly.",
                "DFE", "The DFE Device Foreign Exporter Registration Number should be a numeric string between 1 and 10 digits in length. Please ensure the format complies with these requirements.",
                "DI", "The DI Device Identifier should consist of alphanumeric characters and be between 6 and 23 characters in length.",
                "IDE", "The IDE Investigational Device Exemption Number must adhere to one of the following formats: 4 numeric digits, 5 numeric digits, NSR, or a format beginning with 'G' " +
                        "followed by 6 numeric digits (excluding 'G000000', '0000', and '00000'). Please ensure the number is formatted correctly according to these guidelines.",
                "DA", " The DA New Drug Application Number, Abbreviated New Drug Application Number, or Therapeutic Biologic Application Number must adhere to one of the following formats: BA followed by 4 to 6 numeric digits, " +
                        "BN followed by 5 to 6 numeric digits, or simply a 6-digit numeric string",
                "IND", "The IND (Investigational New Drug) Application Number should consist of a numeric string that is between 4 and 6 digits in length. Please ensure the number adheres to this format.",
                "ACC", "The ACC Product Report Accession Number must be either 7 or 11 characters long. Please ensure the number is formatted correctly with the specified length.",
                "ANC", "The ANC Annual Report Accession Number must be exactly 7 or 11 characters long. Please correct the format to comply with these length requirements."
        ));

        AOCQ_SYNTAX_ERROR_MESSAGES.put("VME", Map.of(
                "REG", "Invalid REG Animal Drug Establishment Registration Number. It must be exactly 9 digits.",
                "VAN", "Invalid VAN Abbreviated New Animal Drug Application Number (ANADA). It must be exactly 6 digits.",
                "VIN", "Invalid VIN Investigational New Animal Drug Number (INAD and JINAD). It must be exactly 6 digits.",
                "VNA", "Invalid VNA Number. The New Animal Drug Application Number (NADA), including CNADA, Type A Medicated Articles, and MIF, must be exactly 6 digits.",
                "NDC", "Invalid NDC (National Drug Code) '123456789'. It must be exactly 10 digits.",
                "MFL", "Invalid format for Medicated Feed Mill License (MFL) number."
        ));
        PARTY_IDENTIFIER_NUMBER_ERROR_MESSAGES.put("16", "Expected Exact 9-digit Party Identifier Number for partyIdentifier type 16");
        PARTY_IDENTIFIER_NUMBER_ERROR_MESSAGES.put("47", "Expected 1-10-digit Party Identifier Number for partyIdentifier type 47");

    }

    public static String getAOCQSyntaxErrorMessage(String programCode, String aoc) {
        if (AOCQ_SYNTAX_ERROR_MESSAGES.containsKey(programCode.toUpperCase())) {
            return AOCQ_SYNTAX_ERROR_MESSAGES.get(programCode.toUpperCase()).get(aoc.toUpperCase());
        }
        return null;
    }

    public static String getPartyIdentifierNumberErrorMessage(String partyIdentifierType) {
        return PARTY_IDENTIFIER_NUMBER_ERROR_MESSAGES.get(partyIdentifierType);
    }
}
