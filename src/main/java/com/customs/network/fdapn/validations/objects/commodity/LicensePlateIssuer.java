package com.customs.network.fdapn.validations.objects.commodity;

import com.customs.network.fdapn.validations.annotations.Regex;
import com.customs.network.fdapn.validations.enums.RegexType;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LicensePlateIssuer {
    @Size(max = 35 , message = "issuerOfLPCO should have a maximum of {max} characters")
    private String issuerOfLPCO;

    @Size(max = 3 , message = "governmentGeographicCodeQualifier should have a maximum of {max} characters")
    @Regex(value = RegexType.ALPHABETIC , message = "governmentGeographicCodeQualifier must be Alphabets")
    private String governmentGeographicCodeQualifier;

    @Size(max = 3 , message = "locationOfIssuerOfTheLPCO should have a maximum of {max} characters")
    @Regex(value = RegexType.ALPHABETIC , message = "locationOfIssuerOfTheLPCO must be Alphabets.")
    private String locationOfIssuerOfTheLPCO;

    @Size(max = 25 , message = "issuingAgencyLocation should have a maximum of {max} characters")
    private String issuingAgencyLocation;
}