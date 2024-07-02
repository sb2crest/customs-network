package com.customs.network.fdapn.validations.objects;

import lombok.Data;

@Data
public class ProductPackaging {
    private int packagingQualifier;
    private int quantity;
    private String uom;
}
