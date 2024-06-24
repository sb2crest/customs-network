package com.customs.network.fdapn.validations.annotations;

import com.customs.network.fdapn.validations.customvalidations.RegexPatternValidator;
import com.customs.network.fdapn.validations.enums.RegexType;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = RegexPatternValidator.class)
public @interface Regex {

    String message() default "Invalid value";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    RegexType value();

    String errorMessage() default "";
}
