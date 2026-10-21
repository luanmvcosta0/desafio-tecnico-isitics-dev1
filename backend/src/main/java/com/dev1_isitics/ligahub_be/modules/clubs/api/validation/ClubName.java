package com.dev1_isitics.ligahub_be.modules.clubs.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Club name with 2 to 5 words and only letters (with or without accent), spaces, hyphens and
 * periods. A {@code null} value is valid; {@code @NotNull} covers absent names.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ClubNameValidator.class)
public @interface ClubName {

  String message() default "deve ter de 2 a 5 palavras, só com letras, espaços, hífen e ponto";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
