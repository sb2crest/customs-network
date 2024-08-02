package com.customs.network.fdapn.validations.objects.priornotice;

import com.customs.network.fdapn.validations.annotations.Regex;
import com.customs.network.fdapn.validations.enums.RegexType;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BillOfLading {
    @Size(min = 1 , max = 1, message = "billTypeIndicatorPE15 must be exactly {max} characters long")
    private String billTypeIndicatorPE15; //re-name to billTypeIndicatorForBillOfLading for more clarity

    @Size(max = 4,message = "issuerCodeOfBillOfLadingNumber maximum {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC, message = "issuerCodeOfBillOfLadingNumber must be an alpha-numeric")
    private String issuerCodeOfBillOfLadingNumber;

    @Size(max = 50,message = "billOfLadingNumber maximum {max} characters")
    @Regex(value = RegexType.ALPHANUMERIC, message = "billOfLadingNumber must be an alpha-numeric")
    private String billOfLadingNumber;

    @Size(max = 21,message = "priorNoticeConfirmationNumber maximum {max} characters")
    private String priorNoticeConfirmationNumber;
}
