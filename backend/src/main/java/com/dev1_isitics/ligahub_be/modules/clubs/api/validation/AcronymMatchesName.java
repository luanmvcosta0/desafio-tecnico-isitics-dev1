package com.dev1_isitics.ligahub_be.modules.clubs.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The 3 letters of the acronym appear in the club name in that order (ignoring case and accents),
 * the first one is the first letter of the name and they are not all equal (RN10).
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = AcronymMatchesNameValidator.class)
public @interface AcronymMatchesName {

  String message() default
      "A sigla (acronym) deve começar pela primeira letra do nome e ter as outras duas letras na"
          + " ordem em que aparecem no nome, sem as três iguais.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
