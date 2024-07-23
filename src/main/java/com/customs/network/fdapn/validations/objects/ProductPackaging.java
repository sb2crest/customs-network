package com.customs.network.fdapn.validations.objects;

import com.customs.network.fdapn.validations.annotations.Regex;
import com.customs.network.fdapn.validations.enums.RegexType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductPackaging {

    @NotNull(message = "Packaging qualifier cannot be null")
    @Size(min = 1, max = 1, message = "Packaging qualifier must be exactly one character")
    @Regex(value = RegexType.NUMERIC, message = "Packaging qualifier must be a number")
    private String packagingQualifier;

    @NotNull(message = "Quantity cannot be null")
    @Regex(value = RegexType.NUMERIC, message = "Quantity must be a number")
    @Size(min = 3, max = 12, message = "Quantity must be between {min} and {max} characters, including 2 decimal places and without special characters")
    private String quantity;

    @NotNull(message = "UOM cannot be null")
    @Size(min = 1, max = 5, message = "UOM must be between {min} and {max} characters")
    private String uom;
}
