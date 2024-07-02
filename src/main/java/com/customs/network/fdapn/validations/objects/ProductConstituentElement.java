package com.customs.network.fdapn.validations.objects;

import lombok.Data;

@Data
public class ProductConstituentElement {
    private String constituentActiveIngredientQualifier;
    private String constituentElementName;
    private String constituentElementQuantity;
    private String constituentElementUnitOfMeasure;
    private String percentOfConstituentElement;
}
