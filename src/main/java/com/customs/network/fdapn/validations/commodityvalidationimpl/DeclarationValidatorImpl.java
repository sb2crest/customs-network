package com.customs.network.fdapn.validations.commodityvalidationimpl;

import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.validations.constants.DeclarationRules;
import com.customs.network.fdapn.validations.objects.priornotice.Declaration;
import io.micrometer.common.util.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.customs.network.fdapn.validations.utils.ErrorUtils.checkInitialViolations;
import static com.customs.network.fdapn.validations.utils.ErrorUtils.createValidationError;

@Component
public class DeclarationValidatorImpl implements DeclarationValidator{
    private final DeclarationRules declarationRules;

    public DeclarationValidatorImpl(DeclarationRules declarationRules) {
        this.declarationRules = declarationRules;
    }

    @Override
    public List<ValidationError> validate(Declaration declaration) {
        List<ValidationError> errors = new ArrayList<>(checkInitialViolations(declaration));
        isValidFields(declaration,errors);
        return errors;
    }
    private void isValidFields(Declaration declaration,List<ValidationError> errors) {
        String actionCode = declaration.getActionCode();
        if(StringUtils.isNotBlank(actionCode) && !declarationRules.isValidActionCode(actionCode)){
            errors.add(createValidationError("actionCode","Invalid Action Code provided for the Declaration",actionCode));
        }

        String filingType = declaration.getFilingType();
        if(StringUtils.isNotBlank(filingType) && !declarationRules.isValidFilingType(filingType)){
            errors.add(createValidationError("filingType","Invalid Filing Type provided for the Declaration",filingType));
        }

        String referenceQualifierCode = declaration.getReferenceQualifierCode();
        if(StringUtils.isNotBlank(referenceQualifierCode) && !declarationRules.isValidReferenceQualifierCode(referenceQualifierCode)){
            errors.add(createValidationError("referenceQualifierCode","Invalid Reference Qualifier Code provided for the Declaration",referenceQualifierCode));
        }

        String entryType =declaration.getEntryType();
        if(StringUtils.isNotBlank(entryType) && !declarationRules.isValidEntryType(entryType)){
            errors.add(createValidationError("entryType","Invalid Entry Type provided for the Declaration",entryType));
        }
        String modeOfTransport = declaration.getModeOfTransportation();
        if (StringUtils.isNotBlank(modeOfTransport) && !declarationRules.isValidaModeOfTransportCode(modeOfTransport)) {
            errors.add(createValidationError("modeOfTransport", "invalid Mode of transport code provided for the Declaration", modeOfTransport));
        }
        String billTypeIndicatorForPE10 = declaration.getBillTypeIndicator();
        if ((StringUtils.isNotBlank(billTypeIndicatorForPE10)) && !declarationRules.isValidBillTypeIndicatorForPE10(billTypeIndicatorForPE10)) {
            errors.add(createValidationError("billTypeIndicatorPE10", "invalid Bill type indicator provided for the Declaration", billTypeIndicatorForPE10));
        }

        String carrier = declaration.getCarrier();
        if (StringUtils.isNotBlank(carrier) && !declarationRules.isValidCarrierCode(carrier)){
            errors.add(createValidationError("carrierCodes","invalid Carrier code provided for the Declaration",carrier));
        }
        declaration.getBillOfLadings().stream()
                .filter(Objects::nonNull)
                .forEach(lading ->{
                    String billTypeIndicatorForPE15 = lading.getBillTypeIndicatorPE15();
                    if (StringUtils.isNotBlank(billTypeIndicatorForPE15) && !declarationRules.isValidBillTypeIndicatorForPE15(billTypeIndicatorForPE15)){
                        errors.add(createValidationError("billTypeIndicatorPE15","invalid Bill type indicator provided for Declaration",billTypeIndicatorForPE15));
                    }
                });
    }
}
