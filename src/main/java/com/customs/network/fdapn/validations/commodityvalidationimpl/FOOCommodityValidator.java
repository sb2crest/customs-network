package com.customs.network.fdapn.validations.commodityvalidationimpl;

import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.validations.CommodityValidator;
import com.customs.network.fdapn.validations.constants.ConditionalValidator;
import com.customs.network.fdapn.validations.constants.FOOCommodityConstants;
import com.customs.network.fdapn.validations.objects.commodity.ProductDetails;

import java.util.ArrayList;
import java.util.List;

public class FOOCommodityValidator extends CommonValidations implements CommodityValidator,SegmentValidator {
    private final ConditionalValidator conditionalValidator;
    private static final String PROGRAMME_CODE = "FOO";

    public FOOCommodityValidator(FOOCommodityConstants fooCommodityConstants) {
        this.conditionalValidator = fooCommodityConstants;
    }
    @Override
    public List<ValidationError> validate(ProductDetails productDetails) {
        List<ValidationError> errors = new ArrayList<>();
        String productCode = productDetails.getProductCodeNumber();
        ValidationContext context =new ValidationContext(productCode,errors,conditionalValidator,PROGRAMME_CODE,productDetails);
        validatePGAIdentifier(context);
        validateProductIdentifier(context);
        validateProductOrigin(context);
        validateProductTradeNames(context);


        return errors;
    }

    @Override
    public ConditionalValidator getConditionalValidator(){
        return null;
    }

    @Override
    public void validateProductConstituentElement(ValidationContext context) {
        //no need to validate
    }
}
