package com.customs.network.fdapn.validations.objects;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AffirmationOfCompliance {
    @Size(max = 5,message = "affirmationComplianceCode can not be greater than {max} characters")
    private String affirmationComplianceCode;
    @Size(max = 30,message = "affirmationComplianceQualifier can not be greater than {max} characters")
    private String affirmationComplianceQualifier;
}
