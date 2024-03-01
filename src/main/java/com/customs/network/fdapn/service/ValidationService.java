package com.customs.network.fdapn.service;

import com.customs.network.fdapn.model.CustomerDetails;
import com.customs.network.fdapn.dto.CustomerFdaPnFailure;
import com.customs.network.fdapn.model.PartyDetails;
import com.customs.network.fdapn.model.ValidationError;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ValidationService {

    private static final String REGEX_PATTERN_FOR_ALPHANUMERIC = "^[a-zA-Z0-9]{%d}$";

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
        if (accountId != null && !accountId.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC, 6))) {
            validationErrorList.add(createValidationError("Acct ID", "Invalid Acct ID. The field should contain 6 alphanumeric character", accountId));
        }

        String userId = customerDetails.getUserId();
        if (userId != null && !userId.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC, 10))) {
            validationErrorList.add(createValidationError("User ID", "Invalid User ID. The field should contain 10 alphanumeric character",userId));
        }

        String modeOfTransportation = customerDetails.getModeOfTrasportation();
        if (modeOfTransportation == null || !modeOfTransportation.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC, 2))) {
            validationErrorList.add(createValidationError("Mode of Transportation", "Invalid Mode of Transportation. The field should contain 2 alphanumeric character",modeOfTransportation));
        }

        String entryType = customerDetails.getEntryType();
        if (entryType == null || !entryType.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC, 2))) {
            validationErrorList.add(createValidationError("Entry Type", "Invalid Entry Type. The field should contain 2 alphanumeric character", entryType));
        }

        String refId = customerDetails.getReferenceIdentifier();
        if (refId == null || !refId.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC, 3))) {
            validationErrorList.add(createValidationError("Reference Identifier", "Invalid Reference Identifier. The field should contain 3 alphanumeric character", refId));
        }

        String refIdNo = customerDetails.getReferenceIdentifierNo();
        if (refIdNo == null || refIdNo.length() > 46) {
            validationErrorList.add(createValidationError("Reference Identifier No", "Invalid Reference Identifier No. The field's length should be less than or equal to 46", refIdNo));
        }

        String filer = customerDetails.getFiler();
        if (filer == null || !filer.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC, 4))) {
            validationErrorList.add(createValidationError("Filer", "Invalid Filer. The field should contain 4 alphanumeric character.", filer));
        }

        String billType = customerDetails.getBillType();
        if (billType == null || !billType.matches(String.format(REGEX_PATTERN_FOR_ALL, 1))) {
            validationErrorList.add(createValidationError("Bill Type", "Invalid Bill Type. The field's length should exactly 1 character", billType));
        }

        String carrier = customerDetails.getCarrier();
        if (carrier == null || !carrier.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC, 4))) {
            validationErrorList.add(createValidationError("Carrier", "Invalid Carrier. The field should contain 4 alphanumeric character.", carrier));
        }

        String billTypeIndicator = customerDetails.getBillTypeIndicator();
        if (billTypeIndicator == null || !billTypeIndicator.matches(String.format(REGEX_PATTERN_FOR_ALL, 1))) {
            validationErrorList.add(createValidationError("Bill Type Indicator", "Invalid Bill Type Indicator. The field's length should exactly 1 character",billTypeIndicator));
        }

        String issuerCode = customerDetails.getIssuerCode();
        if (issuerCode == null || !issuerCode.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC, 4))) {
            validationErrorList.add(createValidationError("Issuer Code", "Invalid Issuer Code. The field should contain 4 alphanumeric character", issuerCode));
        }

        String billOfLandingNumber = customerDetails.getBillingOfLading();
        if (billOfLandingNumber == null || !billOfLandingNumber.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 1, 50))) {
            validationErrorList.add(createValidationError("Bill Of Landing No", "Invalid Bill Of Landing No. The field's length should with in-between 1 and 50", billOfLandingNumber));
        }

        int priorNoticeNumber = customerDetails.getPriorNoticeNumber();
        if (priorNoticeNumber > 1) {
            validationErrorList.add(createValidationError("Prior Notice S No", "Invalid Prior Notice S No. The field's value should be greater than 1", priorNoticeNumber));
        }

        String productNumber = customerDetails.getBillingOfLading();
        if (productNumber == null || !productNumber.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 1, 19))) {
            validationErrorList.add(createValidationError("Product Number", "Invalid Product Number. The field's length should be less than or equal to 19", productNumber));
        }

        String commercialDesc = customerDetails.getCommercialDesc();
        if (commercialDesc == null || !commercialDesc.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 1, 70))) {
            validationErrorList.add(createValidationError("Commercial Desc", "Invalid Commercial Desc. The field's length should be less than or equal to 70", commercialDesc));
        }

        String governmentAgencyProcessingCode = customerDetails.getGovernmentAgencyProcessingCode();
        if (governmentAgencyProcessingCode == null || !governmentAgencyProcessingCode.matches(String.format(REGEX_PATTERN_FOR_ALL, 3))) {
            validationErrorList.add(createValidationError("Government Agency Processing Code", "Invalid Government Agency Processing Code. The field's length should be less than or equal to 3",governmentAgencyProcessingCode));
        }

        String commodityDesc = customerDetails.getCommodityDesc();
        if (commodityDesc == null || !commodityDesc.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 1, 57))) {
            validationErrorList.add(createValidationError("Commodity Desc", "Invalid Commodity Desc. The field's length should be less than or equal to 57",commodityDesc));
        }

        String countryOfProduction = customerDetails.getCountryOfProduction();
        if (countryOfProduction == null || !countryOfProduction.matches(String.format(REGEX_PATTERN_FOR_ALL, 2))) {
            validationErrorList.add(createValidationError("Country Of Production", "Invalid Country Of Production. The field's length should be less than or equal to 2", countryOfProduction));
        }

        String countryOfShipment = customerDetails.getCountryOfShipment();
        if (countryOfShipment == null || !countryOfShipment.matches(String.format(REGEX_PATTERN_FOR_ALL, 2))) {
            validationErrorList.add(createValidationError("Country Of Shipment", "Invalid Country Of Shipment. The field's length should be less than or equal to 2", countryOfShipment));
        }

        String arrivalLocation = customerDetails.getArrivalLocation();
        if (arrivalLocation != null && !arrivalLocation.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 1, 50))) {
            validationErrorList.add(createValidationError("Arrival Location", "Invalid Arrival Location. The field's length should be less than or equal to 50", arrivalLocation));
        }

        String arrivalDate = customerDetails.getArrivalDate();
        if (arrivalDate == null || !isValidDateFormat(arrivalDate)) {
            validationErrorList.add(createValidationError("Arrival Date", "Invalid Arrival Date, must be in DD-MM-YYYY format", arrivalDate));
        }

        int arrivalTime = customerDetails.getArrivalTime();
        if (!isValidTimeFormat(arrivalTime)) {
            validationErrorList.add(createValidationError("Arrival Time", "Invalid arrival time, must be in HHMM format.", arrivalTime));
        }

        String packageTrackingCode = customerDetails.getPackageTrackingCode();
        if (packageTrackingCode != null && !packageTrackingCode.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC, 4))) {
            validationErrorList.add(createValidationError("Package Tracking Code", "Invalid Package Tracking Code. The field should contain 4 alphanumeric character", packageTrackingCode));
        }

        String packageTrackingNumber = customerDetails.getPackageTrackingNumber();
        if (packageTrackingNumber != null && (packageTrackingNumber.length() < 1 || packageTrackingNumber.length() > 50 || !packageTrackingNumber.matches("^[a-zA-Z0-9]+$"))) {
            validationErrorList.add(createValidationError("Package Tracking Number", "Invalid Package Tracking Number. The field should be an alphanumeric value with a length between 1 and 50", packageTrackingNumber));
        }

        String containerNumber = customerDetails.getContainerNumber();
        if (StringUtils.isBlank(containerNumber) || containerNumber.length() > 20 || !containerNumber.matches("^[a-zA-Z0-9]+$")) {
            validationErrorList.add(createValidationError("Container Number", "Invalid container number. It should be an alphanumeric value with a length between 1 and 20.", containerNumber));
        }

        Long baseQuantity = (long) customerDetails.getBaseQuantity();
        if (baseQuantity == null || baseQuantity < 1L || baseQuantity > 999999999999L) {
            validationErrorList.add(createValidationError("Base Quantity", "Invalid Base Quantity. The field should be a numeric value with a range of 1L to 999999999999L", baseQuantity));
        }

        String baseUOM = customerDetails.getBaseUOM();
        if (StringUtils.isBlank(baseUOM) || baseUOM.length() > 5) {
            validationErrorList.add(createValidationError("Base UOM", "Invalid Base UOM. The field should have a length between 1 and 5", baseUOM));
        }

        int packagingQualifier = customerDetails.getPackagingQualifier();
        if (packagingQualifier < 1 || packagingQualifier > 9) {
            validationErrorList.add(createValidationError("Packaging Qualifier", "Invalid Packaging Qualifier. The field should be an numeric value with a range of 1 to 9", packagingQualifier));
        }

        Long quantity = (long) customerDetails.getQuantity();
        if (quantity == null || quantity < 1L || quantity > 999999999999L) {
            validationErrorList.add(createValidationError("Quantity", "Invalid Quantity. The field should be a numeric value with a range of 1L to 999999999999L", quantity));
        }

        String umo = customerDetails.getUOM();
        if (umo == null || !umo.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 1, 5))) {
            validationErrorList.add(createValidationError("UMO", "Invalid UMO. The field should have a length between 1 and 5", umo));
        }

        String affirmationComplianceCode = customerDetails.getAffirmationComplianceCode();
        if (affirmationComplianceCode != null && affirmationComplianceCode.matches(String.format(REGEX_PATTERN_FOR_ALPHA, 3))) {
            validationErrorList.add(createValidationError("Affirmation Compliance Code", "Invalid Affirmation Compliance Code. The field should be an alphabetic character with a length between 1 and 20", affirmationComplianceCode));
        }

        String affirmationComplianceQualifier = customerDetails.getAffirmationComplianceQualifier();
        if (affirmationComplianceQualifier == null || !affirmationComplianceQualifier.matches(String.format(REGEX_PATTERN_FOR_ALPHANUMERIC_WITH_RANGE, 1, 30))) {
            validationErrorList.add(createValidationError("Affirmation Compliance Qualifier", "Invalid Affirmation Compliance Qualifier. The field should be an alphanumeric value with a length between 1 and 30", affirmationComplianceQualifier));
        }

        String end = customerDetails.getEnd();
        if (end == null ) {
            validationErrorList.add(createValidationError("End", "The field should be End", end));
        }

        //For PartyDetails
        customerDetails.getPartyDetails().parallelStream()
                .forEach(p -> validatePartyDetails(p, validationErrorList));


        if (!validationErrorList.isEmpty()) {
            CustomerFdaPnFailure customerFdaPnFailure = new CustomerFdaPnFailure();
            customerFdaPnFailure.setBatchId("");
            customerFdaPnFailure.setUserId(customerDetails.getUserId());
            customerFdaPnFailure.setCreatedOn(String.valueOf(new Date()));
            customerFdaPnFailure.setReferenceIdentifierNo(customerDetails.getReferenceIdentifierNo());
            customerFdaPnFailure.setResponseJson(validationErrorList);
        }

        return validationErrorList;
    }

    private List<ValidationError> validatePartyDetails(PartyDetails p, List<ValidationError> validationErrorList) {
        String partyType = p.getPartyType();
        if (partyType == null || partyType.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 1, 3))) {
            validationErrorList.add(createValidationError("Party Type" + partyType, "Invalid Party Type. The field's length should be in range of 1 and 3",partyType));
        }

        String partyIdentifierType = p.getPartyIdentifierType();
        if (partyIdentifierType != null && partyIdentifierType.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 2, 3))) {
            validationErrorList.add(createValidationError(partyType + "Party Type's " + " Party Identifier Type", "Invalid Party Identifier Type. The field's length should be in range of 2 and 3", partyIdentifierType));
        }

        String partyIdentifierNumber = p.getPartyIdentifierNumber();
        if (partyIdentifierNumber != null && partyIdentifierNumber.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 0, 15))) {
            validationErrorList.add(createValidationError(partyType + "Party Type's " + " Party Identifier Number", "Invalid Party Identifier Number. The field's length should be in range of 0 and 15", partyIdentifierNumber));
        }

        String partyName = p.getPartyName();
        if (partyName == null || partyName.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 1, 32))) {
            validationErrorList.add(createValidationError(partyType + "Party Type's " + " Party Name", "Invalid Party Name. The field's length should be in range of 1 and 32", partyName));
        }

        String address1 = p.getAddress1();
        if (address1 == null || address1.matches(String.format(REGEX_PATTERN_FOR_ALL_WITH_RANGE, 0, 23))) {
            validationErrorList.add(createValidationError(partyType + "Party Type's " + " Address1", "Invalid Address1. The field's length should be in range of 1 and 32", address1));
        }
        return validationErrorList;
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

    private ValidationError createValidationError(String fieldName, String message, Object field) {
        ValidationError validationError = new ValidationError();
        validationError.setFieldName(fieldName);
        validationError.setMessage(message);
        validationError.setActual(field);
        return validationError;
    }
}
