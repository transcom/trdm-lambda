package com.milmove.trdmlambda.milmove.validators;

import com.milmove.trdmlambda.milmove.contraints.ContentUpdatedSinceDateTimeConstraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ContentUpdatedSinceDateTimeValidator
        implements ConstraintValidator<ContentUpdatedSinceDateTimeConstraint, String> {

    @Override
    public boolean isValid(String contentUpdatedSinceDateTime, ConstraintValidatorContext context) {
        return contentUpdatedSinceDateTime != null;
    }
}
