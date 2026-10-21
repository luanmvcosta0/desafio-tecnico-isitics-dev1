package com.dev1_isitics.ligahub_be.modules.clubs.api.validation;

import static com.dev1_isitics.ligahub_be.modules.clubs.domain.ClubLimits.NAME_MAX_WORDS;
import static com.dev1_isitics.ligahub_be.modules.clubs.domain.ClubLimits.NAME_MIN_WORDS;

import com.dev1_isitics.ligahub_be.shared.text.Words;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class ClubNameValidator implements ConstraintValidator<ClubName, String> {

  private static final Pattern ALLOWED_CHARACTERS = Pattern.compile("[\\p{L}\\p{M} .-]+");

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    long words = Words.count(value);
    return ALLOWED_CHARACTERS.matcher(value).matches()
        && words >= NAME_MIN_WORDS
        && words <= NAME_MAX_WORDS;
  }
}
