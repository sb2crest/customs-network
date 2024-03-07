package com.customs.network.fdapn.service;

import com.customs.network.fdapn.dto.CustomerFdaPnFailure;
import com.customs.network.fdapn.model.CustomerDetails;
import com.customs.network.fdapn.model.PartyDetails;
import com.customs.network.fdapn.model.ValidationError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class ValidationService {

    private static final String REGEX_PATTERN_FOR_ALPHANUMERIC = "^(?=.*[a-zA-Z])(?=.*[0-9])[a-zA-Z0-9]{%d}$";

    private static final String REGEX_PATTERN_FOR_ALPHANUMERIC_WITH_RANGE = "^[a-zA-Z0-9]{%d,%d}$";

    private static final String REGEX_PATTERN_FOR_ALPHA = "^[a-zA-Z]{%d}$";

    private static final String DATE_FORMAT_REGEX = "^(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[0-2])-(\\d{4})$";

    private static final String REGEX_PATTERN_FOR_ALL = "^.{%d}$";

    private static final String REGEX_PATTERN_FOR_ALL_WITH_RANGE = "^.{%d,%d}$";

    public List<ValidationError> validateField(List<CustomerDetails> customerDetailsList) {
        List<ValidationError> validationErrors = new ArrayList<>();
        customerDetailsList.parallelStream().forEach(customerDetails -> validationErrors.addAll(validateObject(customerDetails)));
        return validationErrors;
    }

    private List<ValidationError> validateObject(CustomerDetails customerDetails) {
        List<ValidationError> validationErrorList = new ArrayList<>();

        String accountId = customerDetails.getAccountId();
        if (accountId != null && !accountId.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC,6))) {
            validationErrorList.add(createValidationError("Acct ID", "Invalid Acct ID. The field should contain 6 alphanumeric character"));
        }

        String userId = customerDetails.getUserId();
        if (userId == null || userId.isEmpty()) {
            validationErrorList.add(createValidationError("User ID", "User ID is required."));
        } else {
            if (!userId.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC, 10))) {
                validationErrorList.add(createValidationError("User ID", "Invalid User ID. The field should contain 10 alphanumeric characters."));
            }
        }

        String modeOfTransportation = customerDetails.getModeOfTrasportation();
        if (modeOfTransportation == null || !modeOfTransportation.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC,2))) {
            validationErrorList.add(createValidationError("Mode of Transportation", "Invalid Mode of Transportation. The field should contain 2 alphanumeric character"));
        }

        String entryType = customerDetails.getEntryType();
        if (entryType == null || !entryType.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC,2))) {
            validationErrorList.add(createValidationError("Entry Type", "Invalid Entry Type. The field should contain 2 alphanumeric character"));
        }

        String refId = customerDetails.getReferenceIdentifier();
        if (refId == null || !refId.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC,3))) {
            validationErrorList.add(createValidationError("Reference Identifier", "Invalid Reference Identifier. The field should contain 3 alphanumeric character"));
        }

        String refIdNo = customerDetails.getReferenceIdentifierNo();
        if (refIdNo == null || !refIdNo.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE,1,46))) {
            validationErrorList.add(createValidationError("Reference Identifier No", "Invalid Reference Identifier No. The field should have a length between 1 to 46"));
        }

        String filer = customerDetails.getFiler();
        if (filer == null || !filer.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC,4))) {
            validationErrorList.add(createValidationError("Filer", "Invalid Filer. The field should contain 4 alphanumeric character."));
        }

        String billType = customerDetails.getBillType();
        if (billType == null || !billType.matches(String.format(REGEX_PATTERN_FOR_ALL,1))) {
            validationErrorList.add(createValidationError("Bill Type", "Invalid Bill Type. The field should have exactly 1 character length"));
        }

        String carrier = customerDetails.getCarrier();
        if (carrier == null || !carrier.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC,4))) {
            validationErrorList.add(createValidationError("Carrier", "Invalid Carrier. The field should contain 4 alphanumeric character."));
        }

        String billTypeIndicator = customerDetails.getBillTypeIndicator();
        if (billTypeIndicator == null || !billTypeIndicator.matches(String.format(REGEX_PATTERN_FOR_ALL,1))) {
            validationErrorList.add(createValidationError("Bill Type Indicator", "Invalid Bill Type Indicator. The field should have exactly 1 character length"));
        }

        String issuerCode = customerDetails.getIssuerCode();
        if (issuerCode == null || !issuerCode.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC,4))) {
            validationErrorList.add(createValidationError("Issuer Code", "Invalid Issuer Code. The field should contain 4 alphanumeric character"));
        }

        String billOfLandingNumber = customerDetails.getBillingOfLading();
        if (billOfLandingNumber == null || !billOfLandingNumber.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE,0,50))) {
            validationErrorList.add(createValidationError("Bill Of Landing No", "Invalid Bill Of Landing No. The field should have a length between 0 to 50"));
        }

        int priorNoticeNumber = customerDetails.getPriorNoticeNumber();
        if (priorNoticeNumber > 1) {
            validationErrorList.add(createValidationError("Prior Notice S No", "Invalid Prior Notice S No. The field should have a integer type value which is greater than 1"));
        }

        String productNumber = customerDetails.getProductNumber();
        if (productNumber == null || !productNumber.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE,1,19))) {
            validationErrorList.add(createValidationError("Product Number", "Invalid Product Number. The field should have a length between 1 to 19"));
        }

        String commercialDesc = customerDetails.getCommercialDesc();
        if (commercialDesc == null || !commercialDesc.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE,1,70))) {
            validationErrorList.add(createValidationError("Commercial Desc", "Invalid Commercial Desc. The field should have a length between 1 to 70"));
        }

        String governmentAgencyProcessingCode = customerDetails.getGovernmentAgencyProcessingCode();
        if (governmentAgencyProcessingCode == null || !governmentAgencyProcessingCode.matches(String.format(REGEX_PATTERN_FOR_ALL,3))) {
            validationErrorList.add(createValidationError("Government Agency Processing Code", "Invalid Government Agency Processing Code. The field should have exactly 3 character length"));
        }

        String commodityDesc = customerDetails.getCommodityDesc();
        if (commodityDesc == null || !commodityDesc.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE,1,57))) {
            validationErrorList.add(createValidationError("Commodity Desc", "Invalid Commodity Desc. The field should have a length between 1 to 57"));
        }

        String countryOfProduction = customerDetails.getCountryOfProduction();
        if (countryOfProduction == null || !countryOfProduction.matches(String.format(REGEX_PATTERN_FOR_ALL,2))) {
            validationErrorList.add(createValidationError("Country Of Production", "Invalid Country Of Production. The field should have exactly 2 character length"));
        }

        String countryOfShipment = customerDetails.getCountryOfShipment();
        if (countryOfShipment == null || !countryOfShipment.matches(String.format(REGEX_PATTERN_FOR_ALL,2))) {
            validationErrorList.add(createValidationError("Country Of Shipment", "Invalid Country Of Shipment. The field should have exactly 2 character length"));
        }

        String arrivalLocation = customerDetails.getArrivalLocation();
        if (arrivalLocation != null && !arrivalLocation.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE,0,50))) {
            validationErrorList.add(createValidationError("Arrival Location", "Invalid Arrival Location. The field should have a length between 0 to 50"));
        }

        String arrivalDate = customerDetails.getArrivalDate();
        if (arrivalDate == null || !isValidDateFormat(arrivalDate)) {
            validationErrorList.add(createValidationError("Arrival Date", "Invalid Arrival Date, Must be in DD-MM-YYYY format"));
        }

        int arrivalTime = customerDetails.getArrivalTime();
        if (!isValidTimeFormat(arrivalTime)) {
            validationErrorList.add(createValidationError("Arrival Time", "Invalid arrival time, Must be in HHMM format."));
        }

        String packageTrackingCode = customerDetails.getPackageTrackingCode();
        if (packageTrackingCode != null && !packageTrackingCode.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC,4))) {
            validationErrorList.add(createValidationError("Package Tracking Code", "Invalid Package Tracking Code. The field should contain 4 alphanumeric character"));
        }

        String packageTrackingNumber = customerDetails.getPackageTrackingNumber();
        if (packageTrackingNumber != null && !packageTrackingNumber.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC_WITH_RANGE,0,50))) {
            validationErrorList.add(createValidationError("Package Tracking Number", "Invalid Package Tracking Number. The field should be an alphanumeric value with a range of 0 to 50"));
        }

        String containerNumber = customerDetails.getContainerNumber();
        if (containerNumber == null || !containerNumber.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC_WITH_RANGE,0,20))) {
            validationErrorList.add(createValidationError("Container Number", "Invalid Container number. The field should be an alphanumeric value with a length between 1 to 20"));
        }

        // For Party Details
        if( customerDetails.getPartyDetails()!=null && !customerDetails.getPartyDetails().isEmpty()) {
            customerDetails.getPartyDetails().parallelStream()
                    .forEach(p -> validatePartyDetails(p, validationErrorList));
        }

        Long baseQuantity = customerDetails.getBaseQuantity();
        if (baseQuantity == null || baseQuantity < 1L || baseQuantity > 999999999999L) {
            validationErrorList.add(createValidationError("Base Quantity", "Invalid Base Quantity. The field should be a numeric value with a range of 1L to 999999999999L"));
        }

        String baseUOM = customerDetails.getBaseUOM();
        if (baseUOM == null || !baseUOM.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE,1,5))) {
            validationErrorList.add(createValidationError("Base UOM", "Invalid Base UOM. The field should have a length between 1 and 5"));
        }

        int packagingQualifier = customerDetails.getPackagingQualifier();
        if (packagingQualifier < 1 || packagingQualifier > 9) {
            validationErrorList.add(createValidationError("Packaging Qualifier", "Invalid Packaging Qualifier. The field should be a single digit numeric value greater than 0 ( ex: 1 to 9)"));
        }

        Long quantity = customerDetails.getQuantity();
        if (quantity == null || quantity < 1L || quantity > 999999999999L) {
            validationErrorList.add(createValidationError("Quantity", "Invalid Quantity. The field should be a numeric value with a range of 1L to 999999999999L"));
        }

        String umo = customerDetails.getUOM();
        if (umo == null || !umo.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE,1,5))) {
            validationErrorList.add(createValidationError("UMO", "Invalid UMO. The field should have a length between 1 and 5"));
        }

        String affirmationComplianceCode = customerDetails.getAffirmationComplianceCode();
        if (affirmationComplianceCode != null && !affirmationComplianceCode.matches(String.format(REGEX_PATTERN_FOR_ALPHA,3))) {
            validationErrorList.add(createValidationError("Affirmation Compliance Code", "Invalid Affirmation Compliance Code. The field should be an alphabetic character with exactly 3 character length"));
        }

        String affirmationComplianceQualifier = customerDetails.getAffirmationComplianceQualifier();
        if (affirmationComplianceQualifier == null || !affirmationComplianceQualifier.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC_WITH_RANGE,1,30))) {
            validationErrorList.add(createValidationError("Affirmation Compliance Qualifier", "Invalid Affirmation Compliance Qualifier. The field should be an alphanumeric value with a length between 1 and 30"));
        }

        String end = customerDetails.getEnd();
        if (end == null || !end.equalsIgnoreCase("End")) {
            validationErrorList.add(createValidationError("End", "The field should be End"));
        }


        if (!validationErrorList.isEmpty()) {

            log.info("  ******************************************************");
            log.info("errors   {}:",   validationErrorList);
            CustomerFdaPnFailure customerFdpaFailure = new CustomerFdaPnFailure();
            customerFdpaFailure.setBatchId("");
        }

        return validationErrorList;
    }

    private void validatePartyDetails(PartyDetails p, List<ValidationError> validationErrorList) {
        String partyType = p.getPartyType();
        if (partyType == null || !partyType.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE,1,3))) {
            validationErrorList.add(createValidationError("Party Type" + partyType, "Invalid Party Type. The field should have a length between 1 to 3"));
        }

        String partyIdentifierType = p.getPartyIdentifierType();
        if (partyIdentifierType != null && !partyIdentifierType.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE,2,3))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " Party Identifier Type", "Invalid Party Identifier Type. The field should have a length between 2 or 3"));
        }

        String partyIdentifierNumber = p.getPartyIdentifierNumber();
        if (partyIdentifierNumber != null && !partyIdentifierNumber.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE,0,15))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " Party Identifier Number", "Invalid Party Identifier Number. The field should have a length between 0 to 15"));
        }

        String partyName = p.getPartyName();
        if (partyName == null || !partyName.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE,1,32))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " Party Name", "Invalid Party Name. The field should have a length between 1 to 32"));
        }

        String address1 = p.getAddress1();
        if (address1 == null || !address1.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE,0,23))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " Address1", "Invalid Address1. The field should have a length between 0 to 23"));
        }

        String address2 = p.getAddress2();
        if (address2 == null || !address2.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE,0,32))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " Address2", "Invalid Address2. The field should have a length between 0 to 32"));
        }

        String apartmentOrSuiteNo = p.getApartmentOrSuiteNo();
        if (apartmentOrSuiteNo == null || !apartmentOrSuiteNo.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE,0,5))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " Apartment/Suite No", "Invalid Apartment/Suite No. The field should have a length between 0 to 5"));
        }

        String city = p.getCity();
        if (city == null || !city.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE,1,21))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " City", "Invalid City. The field should have a length between 1 to 21"));
        }

        String stateOrProvince = p.getStateOrProvince();
        if (stateOrProvince == null || !stateOrProvince.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE,0,3))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " State/Province", "Invalid State/Province. The field should have a length between 0 to 3"));
        }

        String country = p.getCountry();
        if (country == null || !country.matches(String.format(REGEX_PATTERN_FOR_ALPHA, 2))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " Country", "Invalid Country. The field should be a Alphabetical value with 2 character"));
        }

        String postalCode = p.getPostalCode();
        if (postalCode == null || !postalCode.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE,0,9))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " Postal Code", "Invalid Postal Code. The field should have a length between 0 to 9"));
        }

        String contactPerson = p.getContactPerson();
        if (contactPerson == null || !contactPerson.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE,0,23))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " Contact Person", "Invalid Contact Person. The field should have a length between 0 to 23"));
        }

        String telephoneNumber = p.getTelephoneNumber();
        if (telephoneNumber == null || !telephoneNumber.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE,0,15))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " Telephone Number", "Invalid Telephone Number. The field should have a length between 0 to 15"));
        }

        String email = p.getEmail();
        if (email == null || !email.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE,0,35))) {
            validationErrorList.add(createValidationError(partyType + " Party Type's " + " Email", "Invalid Email. The field should have a length between 0 and 35"));
        }
    }

    public boolean isValidDateFormat(String dateStr) {
        Pattern pattern = Pattern.compile(DATE_FORMAT_REGEX);
        Matcher matcher = pattern.matcher(dateStr);
        return matcher.matches();
    }

    public static boolean isValidTimeFormat(int time) {
        int hours = time / 100;
        int minutes = time % 100;

        return (hours >= 0 && hours <= 23) && (minutes >= 0 && minutes <= 59);
    }

    private ValidationError createValidationError(String fieldName, String message) {
        ValidationError validationError = new ValidationError();
        validationError.setFieldName(fieldName);
        validationError.setMessage(message);
        return validationError;
    }
}
