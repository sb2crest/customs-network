package com.customs.network.fdapn.validations.commodityvalidationimpl;

import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.validations.CommodityValidator;
import com.customs.network.fdapn.validations.constants.BIOCommodityConstants;
import com.customs.network.fdapn.validations.constants.ConditionalValidator;
import com.customs.network.fdapn.validations.objects.commodity.ProductConstituentElement;
import com.customs.network.fdapn.validations.objects.commodity.ProductDetails;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.customs.network.fdapn.utils.UtilMethods.isNullOrEmptyCollection;
import static com.customs.network.fdapn.validations.utils.ErrorUtils.createValidationError;


@Component
@Slf4j
public class BIOCommodityValidator extends CommonValidations implements CommodityValidator, SegmentValidator {
    private static final String PROGRAMME_CODE = "BIO";
    private final ConditionalValidator conditionalValidator;

    public BIOCommodityValidator(BIOCommodityConstants bioCommodityConstants) {
        this.conditionalValidator = bioCommodityConstants;
    }
    @Override
    public ConditionalValidator getConditionalValidator(){
        return conditionalValidator;
    }

    @Override
    public List<ValidationError> validate(ProductDetails productDetails) {
        List<ValidationError> errors = new ArrayList<>();
        String productCode = productDetails.getProductCodeNumber();
        ValidationContext context =new ValidationContext(productCode,errors,conditionalValidator,PROGRAMME_CODE,productDetails,null);
        validateRequiredFields(context);
        validatePGAIdentifier(context);
        validateProductIdentifier(context);
        validateProductConstituentElement(context);
        validateProductOrigin(context);
        validateProductTradeNames(context);
        validatePartyDetails(context);
        validateAffirmationOfCompliance(context);
        validateProductCondition(context);
        validateProductPackaging(context);
        return errors;
    }

    //constituentElementValidation----------------------------------------------------------------
    @Override
    public void validateProductConstituentElement(ValidationContext context) {
        List<ProductConstituentElement> constituentElementsList = context.productDetails().getProductConstituentElements();
        if (isNullOrEmptyCollection(constituentElementsList))
            return;
        for (ProductConstituentElement element : constituentElementsList) {
            String constituentActiveIngredientQualifier = element.getConstituentActiveIngredientQualifier();
            String constituentElementUnitOfMeasure = element.getConstituentElementUnitOfMeasure();
            String percentOfConstituentElement = element.getPercentOfConstituentElement();
            validateConstituentElementFields(constituentActiveIngredientQualifier, constituentElementUnitOfMeasure, percentOfConstituentElement,context);
        }
    }


    private void validateConstituentElementFields(
                                                  String constituentActiveIngredientQualifier,
                                                  String constituentElementUnitOfMeasure,
                                                  String percentOfConstituentElement,
                                                  ValidationContext context) {
        if (StringUtils.isNotBlank(constituentActiveIngredientQualifier) && !context.conditionalValidator().isValidConstituentActiveIngredient(constituentActiveIngredientQualifier)) {
            context.errors().add(createValidationError(context.productCode(), "constituentActiveIngredientQualifier", "If active ingredient is present then this field value should be Y", constituentActiveIngredientQualifier, "Y"));
        }
        if (StringUtils.isBlank(constituentElementUnitOfMeasure) && StringUtils.isBlank(percentOfConstituentElement)) {
            context.errors().add(createValidationError(context.productCode(), "constituentElementUnitOfMeasure", "Either the unit of measure or percent of constituent element should be present", constituentElementUnitOfMeasure, percentOfConstituentElement));
        }
        if (StringUtils.isNotBlank(constituentElementUnitOfMeasure) && !conditionalValidator.isValidUOMCode(constituentElementUnitOfMeasure)) {
            context.errors().add(createValidationError(context.productCode(), "constituentElementUnitOfMeasure", "Provided unit of measure is not valid", constituentElementUnitOfMeasure));
        }
    }


}
