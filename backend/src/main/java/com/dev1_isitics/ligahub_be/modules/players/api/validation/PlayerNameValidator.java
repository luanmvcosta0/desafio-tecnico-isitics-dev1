package com.dev1_isitics.ligahub_be.modules.players.api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Arrays;
import java.util.regex.Pattern;

public class PlayerNameValidator implements ConstraintValidator<PlayerName, String> {

  private static final int MIN_WORDS = 2;
  private static final Pattern DIGIT = Pattern.compile("[0-9]");
  private static final String SPACE = " ";

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    return !DIGIT.matcher(value).find() && countWords(value) >= MIN_WORDS;
  }

  private static long countWords(String value) {
    return Arrays.stream(value.split(SPACE))
        .filter(token -> token.codePoints().anyMatch(Character::isLetter))
        .count();
  }
}
