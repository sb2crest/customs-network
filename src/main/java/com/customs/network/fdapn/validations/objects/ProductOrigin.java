package com.customs.network.fdapn.validations.objects;

import com.customs.network.fdapn.validations.annotations.Regex;
import com.customs.network.fdapn.validations.enums.RegexType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductOrigin {
    @NotNull
    @Size(min = 2, max = 3,message =  "sourceTypeCode length must be between {min} and {max}")
    @Regex(value = RegexType.ALPHANUMERIC,message = "sourceTypeCode must be Alphanumeric")
    private String sourceTypeCode;

    @NotNull
    @Size(min = 2, max = 2,message =  "countryCode length must be {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC,message = "countryCode must be Alphanumeric")
    private String countryCode;
}
