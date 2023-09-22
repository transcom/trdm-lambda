package com.milmove.trdmlambda.milmove.contraints;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.milmove.trdmlambda.milmove.validators.PhysicalNameValidator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Documented
@Constraint(validatedBy = PhysicalNameValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface PhysicalNameConstraint {

    String message() default "invalid physicalName";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}
