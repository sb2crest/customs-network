package com.customs.network.fdapn.validations.objects.commodity;

import com.customs.network.fdapn.validations.annotations.Regex;
import com.customs.network.fdapn.validations.enums.RegexType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AnticipatedArrivalInformations {

    @Size(min = 10, max = 10, message = "Date must be MM-DD-YYYY format")
    @Regex(value = RegexType.MM_DD_YYYY, message = "Date must be MM-DD-YYYY format")
    private String anticipatedArrivalDate;

    @Size(min = 4, max = 4, message = "Time must be HH:MM format")
    @Regex(value = RegexType.HH_MM, message = "Time must be HH:MM format")
    // as of now accept HHMM later HH:MM format, Update the edi for accommodate HH:MM
    private String anticipatedArrivalTime;

    @Size(max = 50, message = "inspectionOrArrivalLocation maximum {max} characters allowed")
    private String inspectionOrArrivalLocation;

    @NotBlank(message = "anticipatedArrivalInformation cannot be empty or null")
    @Size(min = 1, max = 1,message = "anticipatedArrivalInformation must be exactly {max} characters")
    @Regex(value = RegexType.ALPHABETIC,message = "Only Alphabets are allowed")
    private String anticipatedArrivalInformation;

    @Size(max = 4 ,message = "inspectionOrArrivalLocationCode maximum {max} characters allowed")
    @Regex(value = RegexType.ALPHANUMERIC,message = "InspectionOrArrivalLocationCode is alpha-numeric")
    private String inspectionOrArrivalLocationCode;
}
