package com.dev1_isitics.ligahub_be.modules.players.api.validation;

import com.dev1_isitics.ligahub_be.shared.text.Words;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class PlayerNameValidator implements ConstraintValidator<PlayerName, String> {

  private static final int MIN_WORDS = 2;
  private static final Pattern DIGIT = Pattern.compile("[0-9]");

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    return !DIGIT.matcher(value).find() && Words.count(value) >= MIN_WORDS;
  }
}
