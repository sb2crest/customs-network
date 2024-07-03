package com.customs.network.fdapn.validations.commodityvalidationimpl;

import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.validations.CommodityValidator;
import com.customs.network.fdapn.validations.constants.BIOCommodityConstants;
import com.customs.network.fdapn.validations.constants.ConditionalValidator;
import com.customs.network.fdapn.validations.objects.ProductDetails;
import com.customs.network.fdapn.validations.utils.CommonValidations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
@Slf4j
public class BIOCommodityValidator extends CommonValidations implements CommodityValidator,SegmentValidator {
    private static final String PROGRAMME_CODE = "BIO";
    private final ConditionalValidator conditionalValidator;

    public BIOCommodityValidator(BIOCommodityConstants bioCommodityConstants) {
        this.conditionalValidator = bioCommodityConstants;
    }

    @Override
    public List<ValidationError> validate(ProductDetails productDetails) {
        List<ValidationError> errors = new ArrayList<>();
        validatePGAIdentifier(productDetails,errors,conditionalValidator,PROGRAMME_CODE);
        return errors;
    }
    @Override
    public void initialize() {

    }
}
