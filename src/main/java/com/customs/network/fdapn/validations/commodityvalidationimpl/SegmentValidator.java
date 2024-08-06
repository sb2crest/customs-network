package com.customs.network.fdapn.validations.commodityvalidationimpl;

import com.customs.network.fdapn.validations.constants.ConditionalValidator;
import com.customs.network.fdapn.validations.objects.priornotice.Declaration;

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
    void validateAnticipatedArrivalLocation(CommonValidations.ValidationContext context);
    void validateLicensePlateIssuer(CommonValidations.ValidationContext context);
    void validateLicensePlateNumber(CommonValidations.ValidationContext context);
    ConditionalValidator getConditionalValidator();
}
