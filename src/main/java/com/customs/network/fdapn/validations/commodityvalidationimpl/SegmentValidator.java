package com.customs.network.fdapn.validations.commodityvalidationimpl;

import java.util.List;
import java.util.Set;

public interface SegmentValidator {
    void validatePGAIdentifier(CommonValidations.ValidationContext context);
    void validateProductIdentifier(CommonValidations.ValidationContext context);
    void validateProductConstituentElement(CommonValidations.ValidationContext context);
    void validateProductOrigin(CommonValidations.ValidationContext context);
    Set<String> validateAffirmationOfCompliance(CommonValidations.ValidationContext context);
    void validateProductTradeNames(CommonValidations.ValidationContext context);
    Set<String> validatePartyDetails(CommonValidations.ValidationContext context);
    void validateProductCondition(CommonValidations.ValidationContext context);
    void validateProductPackaging(CommonValidations.ValidationContext context);
}
