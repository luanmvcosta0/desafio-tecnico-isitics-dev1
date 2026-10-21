package com.dev1_isitics.ligahub_be.shared.validation;

import com.dev1_isitics.ligahub_be.shared.time.BrasiliaDateTime;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class BrasiliaDateTimeFormatValidator
    implements ConstraintValidator<BrasiliaDateTimeFormat, String> {

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    return value == null || BrasiliaDateTime.parseRequest(value).isPresent();
  }
}
