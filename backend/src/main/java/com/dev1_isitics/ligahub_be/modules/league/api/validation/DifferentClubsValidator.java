package com.dev1_isitics.ligahub_be.modules.league.api.validation;

import com.dev1_isitics.ligahub_be.modules.league.api.MatchRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DifferentClubsValidator implements ConstraintValidator<DifferentClubs, MatchRequest> {

  @Override
  public boolean isValid(MatchRequest request, ConstraintValidatorContext context) {
    return request.homeClubId() == null || !request.homeClubId().equals(request.awayClubId());
  }
}
