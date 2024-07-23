package com.customs.network.fdapn.validations.commodityvalidationimpl;

import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.validations.CommodityValidator;
import com.customs.network.fdapn.validations.constants.COSCommodityConstants;
import com.customs.network.fdapn.validations.constants.ConditionalValidator;
import com.customs.network.fdapn.validations.objects.ProductDetails;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.customs.network.fdapn.validations.utils.ErrorUtils.createValidationError;

@Component
public class COSCommodityValidator extends CommonValidations implements CommodityValidator, SegmentValidator {
    private final ConditionalValidator conditionalValidator;
    private static final String PROGRAMME_CODE = "COS";

    public COSCommodityValidator(COSCommodityConstants cosCommodityValidator) {
        this.conditionalValidator = cosCommodityValidator;
    }

    @Override
    public List<ValidationError> validate(ProductDetails productDetails) {
        List<ValidationError> errors = new ArrayList<>();
        String productCode = productDetails.getProductCodeNumber();
        ValidationContext context = new ValidationContext(productCode, errors, conditionalValidator, PROGRAMME_CODE, productDetails);
        validatePGAIdentifier(context);
        validateProductIdentifier(context);
        validateProductOrigin(context);
        validateProductTradeNames(context);
        validatePartyDetails(context);
        validateAffirmationOfCompliance(context);
        validateProductCondition(context);
        validateProductPackaging(context);
        return errors;
    }

    //PGAIdentifier validation----------------------------------------------------------------
    @Override
    public void validatePGAIdentifier(ValidationContext context) {
        super.validatePGAIdentifier(context);
        String disclaimer = context.productDetails().getDisclaimer();
        if (StringUtils.isBlank(disclaimer)) {
            String processingCode = context.productDetails().getGovernmentAgencyProcessingCode();
            String intendedUseCode = context.productDetails().getIntendedUseCode();
            if (StringUtils.isNotBlank(processingCode)) {
                context.errors().add(
                        createValidationError(context.productCode(), "governmentAgencyProcessingCode",
                                String.format("No Processing code required for commodity %s", PROGRAMME_CODE), processingCode, "null or empty")
                );
            }
            if (StringUtils.isNotBlank(intendedUseCode)) {
                context.errors().add(
                        createValidationError(context.productCode(), "intendedUseCode",
                                String.format("No intended use code required for commodity %s", PROGRAMME_CODE), intendedUseCode, "null or empty")
                );
            }
        }
    }

    @Override
    public void validateProductConstituentElement(ValidationContext context) {
        //no need to validate
    }

    @Override
    public void initialize() {
        // initialize properties if necessary
    }
}
