package com.customs.network.fdapn.validations.objects.commodity;

import com.customs.network.fdapn.validations.annotations.Regex;
import com.customs.network.fdapn.validations.enums.RegexType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductConstituentElement {
    @Size(max = 1, message = "max length of constituentActiveIngredientQualifier is  {max} ")
    @Regex(value = RegexType.ALPHABETIC, message = "Only alphabets are allowed")
    private String constituentActiveIngredientQualifier;

    @NotNull(message = "constituentElementName must not be null")
    @Size(min = 1, max = 51, message = "max length of constituentElementName is  {max} ")
    @Regex(value = RegexType.ALPHANUMERIC_WITH_SPECIAL_CHARS, message = "Invalid pattern of constituentElementName,Allowed AN with special characters")
    private String constituentElementName;

    @Size(min = 3, max = 12, message = "constituentElementQuantity must between {min} - {max} characters,2 decimal places are implied.Example:for 4 quantity value should be 400 in this format")
    @Regex(value = RegexType.NUMERIC, message = "Only Numerics are allowed")
    private String constituentElementQuantity;

    @Size(max = 5,message = "constituentElementUnitOfMeasure should contain maximum {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC, message = "Only alpha-numerics are allowed")
    private String constituentElementUnitOfMeasure;

    @Size(max = 7,message = "percentOfConstituentElement can contain maximum {max} characters,Example : 4 decimal places are implied. 18.2% is entered as 182000.")
    @Regex(value = RegexType.NUMERIC, message = "Only Numerics are allowed")
    private String percentOfConstituentElement;
}
