package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.CustomerFdaPnFailure;
import com.customs.network.fdapn.model.CustomerDetails;
import com.customs.network.fdapn.model.PartyDetails;
import com.customs.network.fdapn.model.ValidationError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class ValidationService {

    private static final String REGEX_PATTERN_FOR_ALPHANUMERIC = "^[a-zA-Z0-9]{%d}$";

    private static final String REGEX_PATTERN_FOR_ALPHANUMERIC_WITH_RANGE = "^[a-zA-Z0-9]{%d,%d}$";

    private static final String REGEX_PATTERN_FOR_ALPHA = "^[a-zA-Z]{%d}$";

    private static final String DATE_FORMAT_REGEX = "^(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[0-2])-(\\d{4})$";

    private static final String REGEX_PATTERN_FOR_ALL = "^.{%d}$";

    private static final String REGEX_PATTERN_FOR_ALL_WITH_RANGE = "^.{%d,%d}$";

    private static final Set<String> MOT_CODES = Set.of(
            "10", "11", "12", "20", "21", "30", "31", "32", "33", "34", "40", "41", "50", "60", "70"
    );

    private static final Set<String> ENTRY_CODES = Set.of(
            "01", "02", "03", "07", "11", "12", "21", "23", "52", "61", "62", "81"
    );

    private static final Set<String> CARRIER_CODES = Set.of("SCAC", "IATA", "UNKN");

    private static final Set<String> Bill_Type_Indicator = Set.of("H", "I", "M", "R", "S", "T");
    private static final Set<String> GOVERNMENT_AGENCY_PROCESSING_CODES = Set.of(
            "ALG", "VAC", "HCT", "XEN", "CGT", "BLO", "BLD", "BDP", "BBA", "PVE",
            "PRE", "OTC", "INV", "PHN", "RND", "NSF", "PRO", "FEE", "ADD", "DSU",
            "CCW", "RED", "NED", "CSU", "FFM", "REP", "ADR", "ADE"
    );
    private static final Set<String> COUNTRY_CODES = Set.of(
            "AF", "AX", "AL", "DZ", "AS", "AD", "AO", "AI", "AQ", "AG", "AR", "AM", "AW", "AU", "AT", "AZ", "BS", "BH",
            "BD", "BB", "BY", "BE", "BZ", "BJ", "BM", "BT", "BO", "BA", "BW", "BV", "BR", "IO", "BN", "BG", "BF", "BI",
            "KH", "CM", "CA", "CV", "KY", "CF", "TD", "CL", "CN", "CX", "CC", "CO", "KM", "CG", "CD", "CK", "CR", "CI",
            "HR", "CU", "CY", "CZ", "DK", "DJ", "DM", "DO", "TP", "EC", "EG", "SV", "GQ", "ER", "EE", "ET", "FK", "FO",
            "FJ", "FI", "FR", "GF", "PF", "TF", "GA", "GM", "GE", "DE", "GH", "GI", "GR", "GL", "GD", "GP", "GU", "GT",
            "GG", "GN", "GW", "GY", "HT", "HM", "VA", "HN", "HK", "HU", "IS", "IN", "ID", "IR", "IQ", "IE", "IM", "IL",
            "IT", "JM", "JP", "JE", "JO", "KZ", "KE", "KI", "KP", "KR", "KW", "KG", "LA", "LV", "LB", "LS", "LR", "LY",
            "LI", "LT", "LU", "MO", "MK", "MG", "MW", "MY", "MV", "ML", "MT", "MH", "MQ", "MR", "MU", "YT", "MX", "FM",
            "MD", "MC", "MN", "ME", "MS", "MA", "MZ", "MM", "NA", "NR", "NP", "NL", "AN", "NC", "NZ", "NI", "NE", "NG",
            "NU", "NF", "MP", "NO", "OM", "PK", "PW", "PS", "PA", "PG", "PY", "PE", "PH", "PN", "PL", "PT", "PR", "QA",
            "RE", "RO", "RU", "RW", "SH", "KN", "LC", "PM", "VC", "WS", "SM", "ST", "SA", "SN", "RS", "SC", "SL", "SG",
            "SK", "SI", "SB", "SO", "ZA", "GS", "ES", "LK", "SD", "SR", "SJ", "SZ", "SE", "CH", "SY", "TW", "TJ", "TZ",
            "TH", "TL", "TG", "TK", "TO", "TT", "TN", "TR", "TM", "TC", "TV", "UG", "UA", "AE", "GB", "US", "UM", "UY",
            "UZ", "VU", "VE", "VN", "VG", "VI", "WF", "EH", "YE", "YU", "ZM", "ZW"
    );

    private static final Set<String> US_STATE_CODES = Set.of(
            "AK", "AL", "AR", "AZ", "CA", "CO", "CT", "DC", "DE", "FL", "GA", "HI", "IA", "ID", "IL", "IN", "KS",
            "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MO", "MS", "MT", "NC", "ND", "NE", "NH", "NJ", "NM", "NV",
            "NY", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VA", "VT", "WA", "WI", "WV", "WY"
    );

    private static final Set<String> MEXICO_STATE_CODES = Set.of(
            "AGU", "BCN", "BCS", "CAM", "CHH", "CHP", "COA", "COL", "DIF", "DUR", "GRO", "GUA", "HID", "JAL", "MEX",
            "MIC", "MOR", "NAY", "NLE", "OAX", "PUE", "QUE", "ROO", "SIN", "SLP", "SON", "TAB", "TAM", "TLA", "VER",
            "YUC", "ZAC"
    );

    private static final Set<String> CANADA_STATE_CODES = Set.of(
            "AB", "BC", "MB", "NB", "NL", "NS", "NT", "NU", "ON", "PE", "QC", "SK", "YT"
    );
    private static final Set<String> BASE_UOM_CODES = Set.of(
            "AU", "BAU", "BBL", "BOL", "CAP", "CAR", "CFT", "CG", "CM", "CM3",
            "CYD", "DOZ", "DPC", "DPR", "FOZ", "FT", "G", "GAL", "GR", "KG",
            "KM", "KM2", "KM3", "L", "LB", "LNM", "M", "M2", "M3", "MCG", "MG",
            "ML", "NO", "OZ", "PCS", "PNU", "PRS", "PTL", "QTL", "SFT", "SQI",
            "STN", "SUP", "SYD", "T", "TAB", "TON", "TOZ", "YD"
    );

    public List<ValidationError> validateField(List<CustomerDetails> customerDetailsList) {
        List<ValidationError> validationErrors = new ArrayList<>();
        customerDetailsList.parallelStream().forEach(customerDetails -> validationErrors.addAll(validateObject(customerDetails)));
        return validationErrors;
    }

    private List<ValidationError> validateObject(CustomerDetails customerDetails) {
        List<ValidationError> validationErrorList = new ArrayList<>();

        String accountId = customerDetails.getAccountId();
        if (accountId != null && !accountId.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC, 6))) {
            validationErrorList.add(createValidationError("Acct ID", "Invalid Acct ID. The field should contain 6 alphanumeric character", accountId));
        }

        String userId = customerDetails.getUserId();
        if (userId == null || userId.isEmpty()) {
            validationErrorList.add(createValidationError("User ID", "User ID is required.", userId));
        } else {
            if (!userId.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC, 10))) {
                validationErrorList.add(createValidationError("User ID", "Invalid User ID. The field should contain 10 alphanumeric characters.", userId));
            }
        }

        String modeOfTransportation = customerDetails.getModeOfTrasportation();
        if (modeOfTransportation.isBlank() || !modeOfTransportation.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC, 2))) {
            validationErrorList.add(createValidationError("Mode of Transportation", "Invalid Mode of Transportation. The field should contain 2 alphanumeric character", modeOfTransportation));
        } else if (!MOT_CODES.contains(modeOfTransportation)) {
            validationErrorList.add(createValidationError("Mode of Transportation", "Invalid code. The code should be one of the allowed Mode Of Transportation codes.", modeOfTransportation));
        }

        String entryType = customerDetails.getEntryType();
        if (entryType.isBlank() || !entryType.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC, 2))) {
            validationErrorList.add(createValidationError("Entry Type", "Invalid Entry Type. The field should contain 2 alphanumeric character", entryType));
        } else if (!ENTRY_CODES.contains(entryType)) {
            validationErrorList.add(createValidationError("Entry Type", "Invalid code. The code should be one of the allowed entry codes.", entryType));
        }

        String refId = customerDetails.getReferenceIdentifier();
        if (refId == null || !refId.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC, 3))) {
            validationErrorList.add(createValidationError("Reference Identifier", "Invalid Reference Identifier. The field should contain 3 alphanumeric character", refId));
        }

        String refIdNo = customerDetails.getReferenceIdentifierNo();
        if (refIdNo == null || !refIdNo.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 1, 46))) {
            validationErrorList.add(createValidationError("Reference Identifier No", "Invalid Reference Identifier No. The field should have a length between 1 to 46", refIdNo));
        }

        String filer = customerDetails.getFiler();
        if (filer == null || !filer.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC, 4))) {
            validationErrorList.add(createValidationError("Filer", "Invalid Filer. The field should contain 4 alphanumeric character.", filer));
        }

        String billType = customerDetails.getBillType();
        if (billType == null || !billType.matches(String.format(REGEX_PATTERN_FOR_ALL, 1))) {
            validationErrorList.add(createValidationError("Bill Type", "Invalid Bill Type. The field should have exactly 1 character length", billType));
        }

        String carrier = customerDetails.getCarrier();
        if (carrier.isBlank() || !carrier.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC, 4))) {
            validationErrorList.add(createValidationError("Carrier", "Invalid Carrier. The field should contain 4 alphanumeric character.", carrier));
        } else if (!CARRIER_CODES.contains(carrier)) {
            validationErrorList.add(createValidationError("Carrier", "Invalid Carrier. The field should be one of the allowed Carrier codes.", carrier));
        }

        String billTypeIndicator = customerDetails.getBillTypeIndicator().toUpperCase();
        if (billTypeIndicator.isBlank() || !billTypeIndicator.matches(String.format(REGEX_PATTERN_FOR_ALL, 1))) {
            validationErrorList.add(createValidationError("Bill Type Indicator", "Invalid Bill Type Indicator. The field should have exactly 1 character length", billTypeIndicator));
        } else if (!Bill_Type_Indicator.contains(billTypeIndicator)) {
            validationErrorList.add(createValidationError("Bill Type Indicator", "Invalid Bill Type Indicator. The field should be one of the allowed Bill Type Indicator.", billTypeIndicator));
        }

        String issuerCode = customerDetails.getIssuerCode();
        if (issuerCode == null || !issuerCode.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC, 4))) {
            validationErrorList.add(createValidationError("Issuer Code", "Invalid Issuer Code. The field should contain 4 alphanumeric character", issuerCode));
        }

        String billOfLandingNumber = customerDetails.getBillingOfLading();
        if (billOfLandingNumber == null || !billOfLandingNumber.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 0, 50))) {
            validationErrorList.add(createValidationError("Bill Of Landing No", "Invalid Bill Of Landing No. The field should have a length between 0 to 50", billOfLandingNumber));
        }

        int priorNoticeNumber = customerDetails.getPriorNoticeNumber();
        if (priorNoticeNumber < 1) {
            validationErrorList.add(createValidationError("Prior Notice S No", "Invalid Prior Notice S No. The field should have an integer type value which is greater than 1", priorNoticeNumber));
        }

        String productNumber = customerDetails.getProductNumber();
        if (productNumber == null || !productNumber.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 1, 19))) {
            validationErrorList.add(createValidationError("Product Number", "Invalid Product Number. The field should have a length between 1 to 19", productNumber));
        }

        String commercialDesc = customerDetails.getCommercialDesc();
        if (commercialDesc == null || !commercialDesc.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 1, 70))) {
            validationErrorList.add(createValidationError("Commercial Desc", "Invalid Commercial Desc. The field should have a length between 1 to 70", commercialDesc));
        }

        String governmentAgencyProcessingCode = customerDetails.getGovernmentAgencyProcessingCode().toUpperCase();
        if (governmentAgencyProcessingCode.isBlank() || !governmentAgencyProcessingCode.matches(String.format(REGEX_PATTERN_FOR_ALL, 3))) {
            validationErrorList.add(createValidationError("Government Agency Processing Code", "Invalid Government Agency Processing Code. The field should have exactly 3 character length", governmentAgencyProcessingCode));
        } else if (!GOVERNMENT_AGENCY_PROCESSING_CODES.contains(governmentAgencyProcessingCode)) {
            validationErrorList.add(createValidationError("Government Agency Processing Code", "Invalid Government Agency Processing Code. The field should be one of the allowed Government Agency Processing Code.", governmentAgencyProcessingCode));
        }

        String commodityDesc = customerDetails.getCommodityDesc();
        if (commodityDesc == null || !commodityDesc.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 1, 57))) {
            validationErrorList.add(createValidationError("Commodity Desc", "Invalid Commodity Desc. The field should have a length between 1 to 57", commodityDesc));
        }

        String countryOfProduction = customerDetails.getCountryOfProduction();
        if (countryOfProduction == null || !countryOfProduction.matches(String.format(REGEX_PATTERN_FOR_ALL, 2))) {
            validationErrorList.add(createValidationError("Country Of Production", "Invalid Country Of Production. The field should have exactly 2 character length", countryOfProduction));
        }

        String countryOfShipment = customerDetails.getCountryOfShipment();
        if (countryOfShipment == null || !countryOfShipment.matches(String.format(REGEX_PATTERN_FOR_ALL, 2))) {
            validationErrorList.add(createValidationError("Country Of Shipment", "Invalid Country Of Shipment. The field should have exactly 2 character length", countryOfShipment));
        }

        String arrivalLocation = customerDetails.getArrivalLocation();
        if (arrivalLocation != null && !arrivalLocation.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 0, 50))) {
            validationErrorList.add(createValidationError("Arrival Location", "Invalid Arrival Location. The field should have a length between 0 to 50", arrivalLocation));
        }

        String arrivalDate = customerDetails.getArrivalDate();
        if (arrivalDate == null || !isValidDateFormat(arrivalDate)) {
            validationErrorList.add(createValidationError("Arrival Date", "Invalid Arrival Date. Must be in DD-MM-YYYY format", arrivalDate));
        }

        String arrivalTime = customerDetails.getArrivalTime();
        if (!isValidMilitaryTime(arrivalTime)) {
            validationErrorList.add(createValidationError("Arrival Time", "Invalid arrival time, Must be in HHMM format.", arrivalTime));
        }

        String packageTrackingCode = customerDetails.getPackageTrackingCode();
        if (packageTrackingCode != null && !packageTrackingCode.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC, 4))) {
            validationErrorList.add(createValidationError("Package Tracking Code", "Invalid Package Tracking Code. The field should contain 4 alphanumeric character", packageTrackingCode));
        }

        String packageTrackingNumber = customerDetails.getPackageTrackingNumber();
        if (packageTrackingNumber != null && !packageTrackingNumber.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC_WITH_RANGE, 0, 50))) {
            validationErrorList.add(createValidationError("Package Tracking Number", "Invalid Package Tracking Number. The field should be an alphanumeric value with a range of 0 to 50", packageTrackingNumber));
        }

        String containerNumber = customerDetails.getContainerNumber();
        if (containerNumber == null || !containerNumber.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC_WITH_RANGE, 0, 20))) {
            validationErrorList.add(createValidationError("Container Number", "Invalid Container number. The field should be an alphanumeric value with a length between 1 to 20", containerNumber));
        }

        // For Party Details
        if (customerDetails.getPartyDetails() != null && !customerDetails.getPartyDetails().isEmpty()) {
            customerDetails.getPartyDetails().parallelStream()
                    .forEach(p -> validatePartyDetails(p, validationErrorList));
        }

        Long baseQuantity = customerDetails.getBaseQuantity();
        if (baseQuantity == null || baseQuantity < 1L || baseQuantity > 999999999999L) {
            validationErrorList.add(createValidationError("Base Quantity", "Invalid Base Quantity. The field should be a numeric value with a range of 1L to 999999999999L", baseQuantity));
        }

        String baseUOM = customerDetails.getBaseUOM();
        if (baseUOM.isBlank() || !baseUOM.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 1, 5))) {
            validationErrorList.add(createValidationError("Base UOM", "Invalid Base UOM. The field should have a length between 1 and 5", baseUOM));
        } else if (!BASE_UOM_CODES.contains(baseUOM.toUpperCase())) {
            validationErrorList.add(createValidationError("Base UOM", "Invalid Base UOM. The field should be one of the allowed Base UOM codes.", baseUOM));
        }

        int packagingQualifier = customerDetails.getPackagingQualifier();
        if (packagingQualifier < 1 || packagingQualifier > 9) {
            validationErrorList.add(createValidationError("Packaging Qualifier", "Invalid Packaging Qualifier. The field should be a single digit numeric value greater than 0 ( ex: 1 to 9)", packagingQualifier));
        }

        Long quantity = customerDetails.getQuantity();
        if (quantity == null || quantity < 1L || quantity > 999999999999L) {
            validationErrorList.add(createValidationError("Quantity", "Invalid Quantity. The field should be a numeric value with a range of 1L to 999999999999L", quantity));
        }

        String uom = customerDetails.getUOM();
        if (uom.isBlank() || !uom.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 1, 5))) {
            validationErrorList.add(createValidationError("UMO", "Invalid UMO. The field should have a length between 1 and 5", uom));
        } else if (!BASE_UOM_CODES.contains(baseUOM.toUpperCase())) {
            validationErrorList.add(createValidationError("UOM", "Invalid UOM. The field should be one of the allowed UOM codes.", uom));
        }

        String affirmationComplianceCode = customerDetails.getAffirmationComplianceCode();
        if (affirmationComplianceCode != null && !affirmationComplianceCode.matches(String.format(REGEX_PATTERN_FOR_ALPHA, 3))) {
            validationErrorList.add(createValidationError("Affirmation Compliance Code", "Invalid Affirmation Compliance Code. The field should be an alphabetic character with exactly 3 character length", affirmationComplianceCode));
        }

        String affirmationComplianceQualifier = customerDetails.getAffirmationComplianceQualifier();
        if (affirmationComplianceQualifier == null || !affirmationComplianceQualifier.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC_WITH_RANGE, 1, 30))) {
            validationErrorList.add(createValidationError("Affirmation Compliance Qualifier", "Invalid Affirmation Compliance Qualifier. The field should be an alphanumeric value with a length between 1 and 30", affirmationComplianceQualifier));
        }

        String end = customerDetails.getEnd();
        if (end == null || !end.equalsIgnoreCase("End")) {
            validationErrorList.add(createValidationError("End", "The field should be End", end));
        }


        if (!validationErrorList.isEmpty()) {

            log.info("  ******************************************************");
            log.info("errors   {}:", validationErrorList);
            CustomerFdaPnFailure customerFdpaFailure = new CustomerFdaPnFailure();
            customerFdpaFailure.setBatchId("");
        }

        return validationErrorList;
    }

    private void validatePartyDetails(PartyDetails p, List<ValidationError> validationErrorList) {
        String partyType = p.getPartyType();
        if (partyType == null || !partyType.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 1, 3))) {
            validationErrorList.add(createValidationError("Party Type", "Invalid Party Type. The field should have a length between 1 to 3", partyType));
        }

        String partyIdentifierType = p.getPartyIdentifierType();
        if (partyIdentifierType != null && !partyIdentifierType.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 2, 3))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " Party Identifier Type", "Invalid Party Identifier Type. The field should have a length between 2 or 3", partyIdentifierType));
        }

        String partyIdentifierNumber = p.getPartyIdentifierNumber();
        if (partyIdentifierNumber != null && !partyIdentifierNumber.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 0, 15))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " Party Identifier Number", "Invalid Party Identifier Number. The field should have a length between 0 to 15", partyIdentifierNumber));
        }

        String partyName = p.getPartyName();
        if (partyName == null || !partyName.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 1, 32))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " Party Name", "Invalid Party Name. The field should have a length between 1 to 32", partyName));
        }

        String address1 = p.getAddress1();
        if (address1 == null || !address1.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 0, 23))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " Address1", "Invalid Address1. The field should have a length between 0 to 23", address1));
        }

        String address2 = p.getAddress2();
        if (address2 == null || !address2.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 0, 32))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " Address2", "Invalid Address2. The field should have a length between 0 to 32", address2));
        }

        String apartmentOrSuiteNo = p.getApartmentOrSuiteNo();
        if (apartmentOrSuiteNo == null || !apartmentOrSuiteNo.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 0, 5))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " Apartment/Suite No", "Invalid Apartment/Suite No. The field should have a length between 0 to 5", apartmentOrSuiteNo));
        }

        String city = p.getCity();
        if (city == null || !city.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 1, 21))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " City", "Invalid City. The field should have a length between 1 to 21", city));
        }

        String stateOrProvince = p.getStateOrProvince();
        if (stateOrProvince == null || !stateOrProvince.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 0, 3))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " State/Province", "Invalid State/Province. The field should have a length between 0 to 3",stateOrProvince));
        } else if (!stateOrProvince.isEmpty()) {
            switch (p.getCountry().toUpperCase()) {
                case "US":
                    if (!US_STATE_CODES.contains(stateOrProvince.toUpperCase())) {
                        validationErrorList.add(createValidationError(partyType + " Party Type's " + " State/Province", "Invalid State/Province. The field should contain a valid US State/Province Code.",stateOrProvince));
                    }
                    break;
                case "MX":
                    if (!MEXICO_STATE_CODES.contains(stateOrProvince.toUpperCase())) {
                        validationErrorList.add(createValidationError(partyType + " Party Type's " + " State/Province", "Invalid State/Province. The field should contain a valid MX State/Province Code.",stateOrProvince));
                    }
                    break;
                case "CA":
                    if (!CANADA_STATE_CODES.contains(stateOrProvince.toUpperCase())) {
                        validationErrorList.add(createValidationError(partyType + " Party Type's " + " State/Province", "Invalid State/Province. The field should contain a valid CA State/Province Code.",stateOrProvince));
                    }
                    break;
            }
        }

        String country = p.getCountry();
        if (country.isBlank() || !country.matches(String.format(REGEX_PATTERN_FOR_ALPHA, 2))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " Country", "Invalid Country. The field should be an alphabetical value with 2 characters", country));
        } else if (!COUNTRY_CODES.contains(country.toUpperCase())) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " Country", "Invalid Country: The field should be one of the allowed country codes.",country));
        }

        String postalCode = p.getPostalCode();
        if (postalCode == null || !postalCode.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 0, 9))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " Postal Code", "Invalid Postal Code. The field should have a length between 0 to 9", postalCode));
        }

        String contactPerson = p.getContactPerson();
        if (contactPerson == null || !contactPerson.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 0, 23))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " Contact Person", "Invalid Contact Person. The field should have a length between 0 to 23", contactPerson));
        }

        String telephoneNumber = p.getTelephoneNumber();
        if (telephoneNumber == null || !telephoneNumber.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 0, 15))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " Telephone Number", "Invalid Telephone Number. The field should have a length between 0 to 15", telephoneNumber));
        }

        String email = p.getEmail();
        if (email == null || !email.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 0, 35))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " Email", "Invalid Email. The field should have a length between 0 and 35", email));
        }
    }

    public boolean isValidDateFormat(String dateStr) {
        Pattern pattern = Pattern.compile(DATE_FORMAT_REGEX);
        Matcher matcher = pattern.matcher(dateStr);
        return matcher.matches();
    }

    public boolean isValidMilitaryTime(String time) {
        if (time == null || !time.matches("([01][0-9]|2[0-3])[0-5][0-9]")) {
            return false;
        }
        int hours = Integer.parseInt(time.substring(0, 2));
        int minutes = Integer.parseInt(time.substring(2));
        return hours >= 0 && hours <= 24 && minutes >= 0 && minutes <= 59;
    }

    public static String convertToAMPMFormat(String militaryTime) {
        int hours = Integer.parseInt(militaryTime.substring(0, 2));
        int minutes = Integer.parseInt(militaryTime.substring(2));

        String suffix = (hours < 12) ? "a.m." : "p.m.";
        if (hours == 0) {
            hours = 12;
        } else if (hours > 12) {
            hours -= 12;
        }
        return String.format("%02d:%02d %s", hours, minutes, suffix);
    }

    private ValidationError createValidationError(String fieldName, String message, Object actual) {
        ValidationError validationError = new ValidationError();
        validationError.setFieldName(fieldName);
        validationError.setMessage(message);
        validationError.setActual(actual);
        return validationError;
    }
}
