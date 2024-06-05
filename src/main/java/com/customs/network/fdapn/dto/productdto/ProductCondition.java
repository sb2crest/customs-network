package com.customs.network.fdapn.dto.productdto;

import lombok.Data;

@Data
public class ProductCondition {
    private String temperatureQualifier;
    private String lotNumberQualifier;
    private String lotNumber;
    private String pgaLineValue;

    // Getters and setters
}
