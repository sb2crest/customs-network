package com.customs.network.fdapn.validations.commodityvalidationImpl;

import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.validations.CommodityValidator;
import com.customs.network.fdapn.validations.objects.ProductDetails;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BIOCommodityValidator implements CommodityValidator {
    @Override
    public List<ValidationError> validate(ProductDetails productDetails) {

        return null;
    }

    @Override
    public void initialize() {

    }
}
