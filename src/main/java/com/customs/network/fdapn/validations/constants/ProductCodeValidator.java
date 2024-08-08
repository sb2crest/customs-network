package com.customs.network.fdapn.validations.constants;

public interface ProductCodeValidator {

    boolean isValidIndustryCode(String processingCode,String industryCode);

    boolean isValidSubClassCode(String processingCode, String industryCode,String subClassCode);
    boolean isLACFProduct(String productCode); // LACF -> low acidified canned food
    boolean isAFProduct(String productCode); // AF -> acidified food
    boolean isInfantFormula(String productCode);

    boolean isValidProcessIndicatorCode(String processingCode, String intendedUseCode,String processIndicatorCode);
}
