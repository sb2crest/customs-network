package com.customs.network.fdapn.validations.objects.commodity;

import com.customs.network.fdapn.validations.annotations.Regex;
import com.customs.network.fdapn.validations.enums.RegexType;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CourierTrackingAndDimensions {
    @Size(min = 4, max = 4, message = "packageTrackingNumberCode must be {max} characters long")
    @Regex(value = RegexType.ALPHANUMERIC, message = "Invalid pattern of characters, expected Alphanumeric characters")
    private String packageTrackingNumberCode;
    @Size(max = 50, message = "packageTrackingNumber must be {max} characters long")
    @Regex(value = RegexType.ALPHANUMERIC, message = "Invalid pattern of characters, expected Alphanumeric characters")
    private String packageTrackingNumber;
    @Size(min = 4, max = 4, message = "containerDimensionsOne must be {max} characters long")
    @Regex(value = RegexType.NUMERIC, message = "Invalid pattern of characters, expected numeric characters")
    private String containerDimensionsOne;
    @Size(min = 4, max = 4, message = "containerDimensionsTwo must be {max} characters long")
    @Regex(value = RegexType.NUMERIC, message = "Invalid pattern of characters, expected numeric characters")
    private String containerDimensionsTwo;
    @Size(min = 4, max = 4, message = "containerDimensionsThree must be {max} characters long")
    @Regex(value = RegexType.NUMERIC, message = "Invalid pattern of characters, expected numeric characters")
    private String containerDimensionsThree;
}
