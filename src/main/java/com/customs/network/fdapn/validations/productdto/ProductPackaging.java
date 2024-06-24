package com.customs.network.fdapn.validations.productdto;

import lombok.Data;

@Data
public class ProductPackaging {
    private int packagingQualifier;
    private int quantity;
    private String uom;

    // Getters and setters
}
