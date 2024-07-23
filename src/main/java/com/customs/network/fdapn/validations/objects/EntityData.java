package com.customs.network.fdapn.validations.objects;

import com.customs.network.fdapn.validations.annotations.Regex;
import com.customs.network.fdapn.validations.enums.RegexType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
 public class EntityData {
    @Size(max = 23, message = "address1 must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, errorMessage = "Invalid pattern of characters")
    private String address1;

    @NotNull(message = "partyName must not be null")
    @Size(min = 1, max = 32, message = "partyName must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, errorMessage = "Invalid pattern of characters")
    private String partyName;

    @NotNull(message = "partyType must not be null")
    @Size(min = 1, max = 3, message = "partyName must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, errorMessage = "Invalid pattern of characters")
    private String partyType;

    @NotNull(message = "partyIdentifierType must not be null")
    @Size(min=2,max = 3, message = "partyIdentifierType must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, errorMessage = "Invalid pattern of characters")
    private String partyIdentifierType;

    @Size(max = 15, message = "partyIdentifierNumber must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, errorMessage = "Invalid pattern of characters")
    private String partyIdentifierNumber;
    private List<AdditionalInformations> additionalInformations;
}