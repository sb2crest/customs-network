package com.customs.network.fdapn.utils;

import java.util.HashSet;
import java.util.Set;

public class DefinedFieldsJson {
    private  static Set<String> definedFields = new HashSet<>();
    static {
        definedFields.add("commercialDesc");
        definedFields.add("pgaLineNumber");
        definedFields.add("governmentAgencyCode");
        definedFields.add("governmentAgencyProgramCode");
        definedFields.add("governmentAgencyProcessingCode");
        definedFields.add("intendedUseCode");
        definedFields.add("intendedUseDescription");
        definedFields.add("correctionIndicator");
        definedFields.add("disclaimer");
        definedFields.add("itemType");
        definedFields.add("productCodeQualifier");
        definedFields.add("sourceCodeType");
        definedFields.add("productCodeNumber");
        definedFields.add("constituentActiveIngredientQualifier");
        definedFields.add("constituentElementName");
        definedFields.add("constituentElementQuantity");
        definedFields.add("constituentElementUnitOfMeasure");
        definedFields.add("percentOfConstituentElement");
        definedFields.add("sourceTypeCode");
        definedFields.add("countryCode");
        definedFields.add("tradeOrBrandName");
        definedFields.add("commodityDesc");
        definedFields.add("issuerOfLPCO");
        definedFields.add("governmentGeographicCodeQualifier");
        definedFields.add("locationOfIssuerOfTheLPCO");
        definedFields.add("issuingAgencyLocation");
        definedFields.add("transactionType");
        definedFields.add("lpcoOrCodeType");
        definedFields.add("lpcoOrPncNumber");
        definedFields.add("partyType");
        definedFields.add("partyIdentifierType");
        definedFields.add("partyIdentifierNumber");
        definedFields.add("partyName");
        definedFields.add("address1");
        definedFields.add("address2");
        definedFields.add("apartmentOrSuiteNo");
        definedFields.add("city");
        definedFields.add("stateOrProvince");
        definedFields.add("country");
        definedFields.add("postalCode");
        definedFields.add("individualQualifier");
        definedFields.add("contactPerson");
        definedFields.add("telephoneNumber");
        definedFields.add("email");
        definedFields.add("affirmationComplianceCode");
        definedFields.add("affirmationComplianceQualifier");
        definedFields.add("remarksTypeCode");
        definedFields.add("remarksText");
        definedFields.add("temperatureQualifier");
        definedFields.add("degreeType");
        definedFields.add("negativeNumber");
        definedFields.add("actualTemperature");
        definedFields.add("locationOfTemperatureRecording");
        definedFields.add("lotNumberQualifier");
        definedFields.add("lotNumber");
        definedFields.add("productionStartDate");
        definedFields.add("productionEndDate");
        definedFields.add("pgaLineValue");
        definedFields.add("packagingQualifier");
        definedFields.add("quantity");
        definedFields.add("uom");
        definedFields.add("containerNumberOne");
        definedFields.add("containerNumberTwo");
        definedFields.add("containerNumberThree");
        definedFields.add("containerDimensionsOne");
        definedFields.add("containerDimensionsTwo");
        definedFields.add("containerDimensionsThree");
        definedFields.add("packageTrackingNumberCode");
        definedFields.add("packageTrackingNumber");
        definedFields.add("anticipatedArrivalInformation");
        definedFields.add("arrivalDate");
        definedFields.add("arrivalTime");
        definedFields.add("anticipatedArrivalLocationCode");
        definedFields.add("arrivalLocation");
        definedFields.add("additionalInformationQualifierCode");
        definedFields.add("additionalInformation");
        definedFields.add("substitutionIndicator");
        definedFields.add("substitutionNumber");

    }
    public static boolean isValidField(String fieldName){
        return definedFields.contains(fieldName);
    }
}
