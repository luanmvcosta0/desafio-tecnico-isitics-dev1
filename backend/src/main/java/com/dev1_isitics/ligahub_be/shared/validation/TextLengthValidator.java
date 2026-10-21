package com.dev1_isitics.ligahub_be.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class TextLengthValidator implements ConstraintValidator<TextLength, String> {

  private int min;
  private int max;

  @Override
  public void initialize(TextLength constraint) {
    min = constraint.min();
    max = constraint.max();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) {
      return true;
    }
    long length = value.codePoints().count();
    return length >= min && length <= max;
  }
}
