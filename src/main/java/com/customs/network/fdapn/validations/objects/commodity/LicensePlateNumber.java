package com.customs.network.fdapn.validations.objects.commodity;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LicensePlateNumber {
    @Size(max = 1, message = "transactionType must be exact {max} characters")
    private String transactionType;
    @Size(max = 3, message = "lpcoOrCodeType should have a maximum of {max} characters")
    private String lpcoOrCodeType; //lpco (l - licenses, p - permits, c -certificates, o - others)
    @Size(max = 33, message = "lpcoOrPncNumber should have a maximum of {max} characters")
    private String lpcoOrPncNumber;
}