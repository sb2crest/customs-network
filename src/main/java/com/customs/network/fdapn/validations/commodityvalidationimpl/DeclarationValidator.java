package com.customs.network.fdapn.validations.commodityvalidationimpl;

import com.customs.network.fdapn.model.ValidationError;
import com.customs.network.fdapn.validations.objects.priornotice.Declaration;

import java.util.List;

public interface DeclarationValidator {
    List<ValidationError> validate(Declaration declaration);
}
