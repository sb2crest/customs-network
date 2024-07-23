package com.customs.network.fdapn.validations.objects;

import com.customs.network.fdapn.validations.annotations.Regex;
import com.customs.network.fdapn.validations.enums.RegexType;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductCondition {

    @Size(min = 1, max = 1, message = "Temperature qualifier must be exactly {max} character")
    @Regex(value = RegexType.ALPHABETIC, message = "Temperature qualifier must be an Alphabet character")
    private String temperatureQualifier;

    @Size(min = 1, max = 1, message = "lotNumberQualifier must be exactly {max} character")
    @Regex(value = RegexType.ALPHABETIC, message = "lotNumberQualifier must be an Alphabet character")
    private String lotNumberQualifier;

    @Size(max = 25, message = "lotNumber cannot be more than {max} characters")
    private String lotNumber;

    @Size(max = 12, message = "pgaLineValue cannot be more than {max} characters")
    @Regex(value = RegexType.NUMERIC, message = "pgaLineValue must be a number")
    private String pgaLineValue;

    @Size(min = 1, max = 1, message = "degreeType must be exactly {max} characters")
    @Regex(value = RegexType.ALPHABETIC, message = "degreeType must be an alphabet")
    private String degreeType;

    @Size(min = 1, max = 1, message = "negativeNumber must be exactly {max} characters")
    @Regex(value = RegexType.ALPHABETIC, message = "negativeNumber must be an alphabet")
    private String negativeNumber;

    @Size(min = 3, max = 6, message = "actualTemperature must be between {min} and {max}, Example : For actual temperature 1, input should be 100 (2 decimal position implied)")
    @Regex(value = RegexType.NUMERIC, message = "actualTemperature must be a number")
    private String actualTemperature;

    @Size(min = 1, max = 1, message = "locationOfTemperatureRecording must be exactly {max} characters")
    @Regex(value = RegexType.ALPHABETIC, message = "locationOfTemperatureRecording must be an alphabet")
    private String locationOfTemperatureRecording;

    @Size(min = 10, max = 10, message = "productionStartDate must be exactly {max} characters")
    @Regex(value = RegexType.DATE, message = "productionStartDate must be a valid date in the format of dd-mm-yyyy")
    private String productionStartDate;

    @Size(min = 10, max = 10, message = "productionStartDate must be exactly {max} characters")
    @Regex(value = RegexType.DATE, message = "productionStartDate must be a valid date in the format of dd-mm-yyyy")
    private String productionEndDate;

}
