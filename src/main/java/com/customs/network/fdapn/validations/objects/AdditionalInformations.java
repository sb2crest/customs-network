package com.customs.network.fdapn.validations.objects;

import com.customs.network.fdapn.validations.annotations.Regex;
import com.customs.network.fdapn.validations.enums.RegexType;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdditionalInformations {
    @Size(min = 3,max = 3,message = "additionalInformationQualifierCode must be {max} characters long")
    @Regex(value = RegexType.ALPHANUMERIC, message = "Invalid pattern of characters,Expected Alphanumeric")
    private String additionalInformationQualifierCode;

    @Size(max = 73,message = "additionalInformation can have maximum of {max} characters, If required use another additionalInformation object")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, message = "Invalid pattern of characters, expected Alphanumeric with special characters")
    private String additionalInformation;
}
