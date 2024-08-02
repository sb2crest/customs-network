package com.customs.network.fdapn.validations.objects.commodity;

import com.customs.network.fdapn.validations.annotations.Regex;
import com.customs.network.fdapn.validations.enums.RegexType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class TradeOrBrandNameInfo {
    @Size(max =35, message = "tradeOrBrandName can hold maximum {max} characters. If your value is more than {max} characters , Use additionalInformation field for additional informations")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS,message = "invalid sequence of characters")
    private String tradeOrBrandName;

    @Valid
    private List<AdditionalInformations> additionalInformations;
}
