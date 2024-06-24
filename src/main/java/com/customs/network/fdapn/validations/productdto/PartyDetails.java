package com.customs.network.fdapn.validations.productdto;

import com.customs.network.fdapn.validations.annotations.Regex;
import com.customs.network.fdapn.validations.enums.RegexType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PartyDetails {

    @NotNull(message = "city must not be null")
    @Size(min = 1, max = 21, message = "city must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, errorMessage = "Invalid pattern of characters")
    private String city;

    @NotNull(message ="email must not be empty")
    @Size(min=1,max = 35, message = "email must be between {min} and {max} characters")
    @Regex(value = RegexType.EMAIL, errorMessage = "Invalid Structure of mail address")
    private String email;

    @NotNull(message = "country must not be null")
    @Size(min = 2, max = 2, message = "country must be exact {max} characters")
    @Regex(value = RegexType.ALPHABETIC, errorMessage = "Invalid pattern of characters")
    private String country;

    @Size(max = 23, message = "address1 must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, errorMessage = "Invalid pattern of characters")
    private String address1;

    @Size(max = 32, message = "address2 must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, errorMessage = "Invalid pattern of characters")
    private String address2;

    @NotNull(message = "partyName must not be null")
    @Size(min = 1, max = 32, message = "partyName must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, errorMessage = "Invalid pattern of characters")
    private String partyName;
    @NotNull(message = "individualQualifier must not be null")

    @Size(min = 1, max = 3, message = "individualQualifier must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, errorMessage = "Invalid pattern of characters")
    @NotBlank(message = "individualQualifier must not be null")
    private String individualQualifier;

    @NotNull(message = "partyType must not be null")
    @Size(min = 1, max = 3, message = "partyName must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, errorMessage = "Invalid pattern of characters")
    private String partyType;

    @Size(max = 9, message = "postalCode must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, errorMessage = "Invalid pattern of characters")
    private String postalCode;

    @Size(max = 23, message = "contactPerson must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, errorMessage = "Invalid pattern of characters")
    private String contactPerson;

    @Size(max = 3, message = "stateOrProvince must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC, errorMessage = "Invalid pattern of characters")
    private String stateOrProvince;

    @NotNull(message = "telephoneNumber must not be null")
    @Size(min = 1, max = 15, message = "telephoneNumber must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, errorMessage = "Invalid pattern of characters")
    private String telephoneNumber;

    @Size(max = 5, message = "telephoneNumber must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, errorMessage = "Invalid pattern of characters")
    private String apartmentOrSuiteNo;

    @NotNull(message = "partyIdentifierType must not be null")
    @Size(min=2,max = 3, message = "partyIdentifierType must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, errorMessage = "Invalid pattern of characters")
    private String partyIdentifierType;

    @Size(max = 15, message = "partyIdentifierNumber must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, errorMessage = "Invalid pattern of characters")
    private String partyIdentifierNumber;
}
