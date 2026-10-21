package com.dev1_isitics.ligahub_be.modules.league.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Home and away clubs of a match must be different (RN20: 400). */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DifferentClubsValidator.class)
public @interface DifferentClubs {

  String message() default "O mandante e o visitante devem ser clubes diferentes.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
