package com.customs.network.fdapn.validations.objects;

import lombok.Data;

@Data
public class ProductCondition {
    private String temperatureQualifier;
    private String lotNumberQualifier;
    private String lotNumber;
    private String pgaLineValue;
}
