package com.customs.network.fdapn.validations.commodityvalidationImpl;

import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.validations.CommodityValidator;
import com.customs.network.fdapn.validations.objects.ProductDetails;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class COSCommodityValidator implements CommodityValidator {
    @Override
    public List<ValidationError> validate(ProductDetails productDetails) {
        return new ArrayList<>();
    }

    @Override
    public void initialize() {

    }
}
