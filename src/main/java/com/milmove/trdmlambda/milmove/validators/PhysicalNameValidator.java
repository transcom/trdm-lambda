package com.milmove.trdmlambda.milmove.validators;

import com.milmove.trdmlambda.milmove.contraints.PhysicalNameConstraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PhysicalNameValidator implements ConstraintValidator<PhysicalNameConstraint, String> {

    @Override
    public boolean isValid(String physicalName, ConstraintValidatorContext context) {
       return physicalName != null;
    }
    
}
