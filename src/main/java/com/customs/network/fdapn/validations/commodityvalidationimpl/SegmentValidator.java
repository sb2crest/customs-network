package com.customs.network.fdapn.validations.commodityvalidationimpl;

import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.validations.constants.ConditionalValidator;
import com.customs.network.fdapn.validations.objects.ProductDetails;

import java.util.List;

public interface SegmentValidator {
    void validatePGAIdentifier(ProductDetails productDetails, List<ValidationError> errors, ConditionalValidator conditionalValidator, String programCode);
}
