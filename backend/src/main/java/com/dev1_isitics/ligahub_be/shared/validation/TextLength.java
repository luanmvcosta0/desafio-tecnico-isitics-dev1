package com.dev1_isitics.ligahub_be.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Inclusive text size counted in Unicode code points (V4), unlike {@code @Size}, which counts UTF-16
 * units. A {@code null} value is valid; combine with {@code @NotNull} when the field is required.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TextLengthValidator.class)
public @interface TextLength {

  int min();

  int max();

  String message() default "deve ter entre {min} e {max} caracteres";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
