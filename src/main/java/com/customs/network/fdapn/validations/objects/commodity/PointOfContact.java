package com.customs.network.fdapn.validations.objects.commodity;

import com.customs.network.fdapn.validations.annotations.Regex;
import com.customs.network.fdapn.validations.enums.RegexType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class PointOfContact {
    @Size(max = 23, message = "contactPerson must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, errorMessage = "Invalid pattern of characters")
    private String contactPerson;

    @NotNull(message = "email must not be empty")
    @Size(min = 1, max = 35, message = "email must be between {min} and {max} characters")
    @Regex(value = RegexType.EMAIL, errorMessage = "Invalid Structure of mail address")
    private String email;

    @NotNull(message = "telephoneNumber must not be null")
    @Size(min = 1, max = 15, message = "telephoneNumber must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, errorMessage = "Invalid pattern of characters")
    private String telephoneNumber;

    @NotNull(message = "individualQualifier must not be null")
    @Size(min = 1, max = 3, message = "individualQualifier must be between {min} and {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, errorMessage = "Invalid pattern of characters")
    private String individualQualifier;

    @Valid
    private List<AdditionalInformations> additionalInformations;
}