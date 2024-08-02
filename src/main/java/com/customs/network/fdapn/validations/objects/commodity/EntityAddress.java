package com.customs.network.fdapn.validations.objects.commodity;

import com.customs.network.fdapn.validations.annotations.Regex;
import com.customs.network.fdapn.validations.enums.RegexType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class EntityAddress {

    @Size(max = 32, message = "address2 must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, errorMessage = "Invalid pattern of characters")
    private String address2;

    @Size(max = 9, message = "postalCode must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, errorMessage = "Invalid pattern of characters")
    private String postalCode;

    @Size(max = 3, message = "stateOrProvince must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC, errorMessage = "Invalid pattern of characters")
    private String stateOrProvince;

    @Size(max = 5, message = "telephoneNumber must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, errorMessage = "Invalid pattern of characters")
    private String apartmentOrSuiteNo;

    @NotNull(message = "city must not be null")
    @Size(min = 1, max = 21, message = "city must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, errorMessage = "Invalid pattern of characters")
    private String city;

    @NotNull(message = "country must not be null")
    @Size(min = 2, max = 2, message = "country must be exact {max} characters")
    @Regex(value = RegexType.ALPHABETIC, errorMessage = "Invalid pattern of characters")
    private String country;

    private List<AdditionalInformations> additionalInformations;
}