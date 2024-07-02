package com.customs.network.fdapn.validations;

import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.validations.objects.ProductDetails;

import java.util.List;

public interface CommodityValidator {
    List<ValidationError> validate(ProductDetails productDetails);
    void initialize();
}
