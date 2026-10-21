package com.dev1_isitics.ligahub_be.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Date-time written exactly as {@code AAAA-MM-DDTHH:MM-03:00} and existing in the calendar (RN01).
 * A {@code null} value is valid; combine with {@code @NotNull} when the field is required.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = BrasiliaDateTimeFormatValidator.class)
public @interface BrasiliaDateTimeFormat {

  String message() default
      "deve ser uma data e hora existente no formato AAAA-MM-DDTHH:MM-03:00 (horário de Brasília)";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
