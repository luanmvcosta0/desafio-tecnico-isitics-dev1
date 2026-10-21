package com.dev1_isitics.ligahub_be.modules.players.api.validation;

import static com.dev1_isitics.ligahub_be.modules.players.domain.PlayerLimits.VETERAN_AGILITY_MAX;
import static com.dev1_isitics.ligahub_be.modules.players.domain.PlayerLimits.VETERAN_AGILITY_MIN_AGE;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AgilityAllowedForAgeValidator
    implements ConstraintValidator<AgilityAllowedForAge, AgeAndAgility> {

  @Override
  public boolean isValid(AgeAndAgility request, ConstraintValidatorContext context) {
    if (request.age() == null || request.agility() == null) {
      return true;
    }
    return request.age() < VETERAN_AGILITY_MIN_AGE || request.agility() <= VETERAN_AGILITY_MAX;
  }
}
