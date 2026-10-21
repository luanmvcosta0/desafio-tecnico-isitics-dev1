package com.dev1_isitics.ligahub_be.modules.players.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Players aged 35 or more have agility of at most 85. */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AgilityAllowedForAgeValidator.class)
public @interface AgilityAllowedForAge {

  String message() default "Com 35 anos ou mais, a agilidade (agility) deve ser no máximo 85.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
