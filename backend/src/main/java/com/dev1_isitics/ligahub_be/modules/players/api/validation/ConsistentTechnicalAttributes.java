package com.dev1_isitics.ligahub_be.modules.players.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Cross rules of the registration attributes: the six sum at most 480, the potential is at least
 * the highest of them and, at 33 or more, equal to it.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ConsistentTechnicalAttributesValidator.class)
public @interface ConsistentTechnicalAttributes {

  String message() default "Os indicadores técnicos são incompatíveis entre si.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
