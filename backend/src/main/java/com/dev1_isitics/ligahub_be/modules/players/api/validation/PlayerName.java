package com.dev1_isitics.ligahub_be.modules.players.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Player name with at least two words (sequences without spaces holding a letter) and no digit. A
 * {@code null} value is valid; {@code @NotNull} covers absent names.
 */
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PlayerNameValidator.class)
public @interface PlayerName {

  String message() default "deve ter pelo menos duas palavras e nenhum dígito";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
