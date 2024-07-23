package com.customs.network.fdapn.validations.constants;

public interface ProductCodeValidator {

    boolean isValidIndustryCode(String processingCode,String industryCode);

    boolean isValidSubClassCode(String processingCode, String industryCode,String subClassCode);

    boolean isValidProcessIndicatorCode(String processingCode, String intendedUseCode,String processIndicatorCode);
}
