package com.customs.network.fdapn.validations.annotations;

import com.customs.network.fdapn.validations.enums.RegexType;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class RegexPatternValidator implements ConstraintValidator<Regex, String> {

    private RegexType regexType;
    private String customErrorMessage;

    @Override
    public void initialize(Regex constraintAnnotation) {
        this.regexType = constraintAnnotation.value();
        this.customErrorMessage = constraintAnnotation.errorMessage();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        boolean isValid = switch (regexType) {
            case NUMERIC -> value.matches("\\d+");
            case ALPHABETIC_WITH_SPACE -> value.matches("^[A-Za-z\\s]+$");
            case ALPHANUMERIC -> value.matches("[A-Za-z0-9\\s]*");
            case NUMERIC_WITH_HYPHEN -> value.matches("^[0-9\\-]+$");
            case NUMERIC_WITH_SPECIAL_CHARS -> value.matches("^[0-9\\s!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]+$");
            case ALPHABETIC -> value.matches("[A-Za-z]+");
            case ALPHANUMERIC_WITH_SPECIAL_CHARS -> value.matches("[A-Za-z0-9 !@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]*");
            case EMAIL -> value.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
            case DATE -> value.matches("^(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[0-2])-(\\d{4})$");
        };

        if (!isValid) {
            buildConstraintViolation(context, customErrorMessage.isEmpty() ? "Invalid value" : customErrorMessage);
        }

        return isValid;
    }

    private void buildConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}
