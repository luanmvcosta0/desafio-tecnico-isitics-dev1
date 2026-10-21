package com.dev1_isitics.ligahub_be.modules.players.api.validation;

import static com.dev1_isitics.ligahub_be.modules.players.domain.PlayerLimits.ATTRIBUTES_MAX_SUM;
import static com.dev1_isitics.ligahub_be.modules.players.domain.PlayerLimits.FIXED_POTENTIAL_MIN_AGE;

import com.dev1_isitics.ligahub_be.modules.players.api.PlayerCreateRequest;
import com.dev1_isitics.ligahub_be.modules.players.domain.TechnicalAttributes;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.stream.Stream;

public class ConsistentTechnicalAttributesValidator
    implements ConstraintValidator<ConsistentTechnicalAttributes, PlayerCreateRequest> {

  private static final String SUM_MESSAGE =
      "A soma dos seis indicadores técnicos deve ser no máximo " + ATTRIBUTES_MAX_SUM + ".";
  private static final String POTENTIAL_BELOW_MESSAGE =
      "O potencial (potential) deve ser maior ou igual ao maior dos seis indicadores (%d).";
  private static final String POTENTIAL_FIXED_MESSAGE =
      "Com "
          + FIXED_POTENTIAL_MIN_AGE
          + " anos ou mais, o potencial (potential) deve ser igual ao maior dos seis"
          + " indicadores (%d).";

  @Override
  public boolean isValid(PlayerCreateRequest request, ConstraintValidatorContext context) {
    if (hasMissingValue(request)) {
      return true;
    }
    TechnicalAttributes attributes = request.toAttributes();
    int highest = attributes.highest();
    if (attributes.sum() > ATTRIBUTES_MAX_SUM) {
      return reject(context, SUM_MESSAGE);
    }
    if (request.potential() < highest) {
      return reject(context, POTENTIAL_BELOW_MESSAGE.formatted(highest));
    }
    if (request.age() != null
        && request.age() >= FIXED_POTENTIAL_MIN_AGE
        && request.potential() != highest) {
      return reject(context, POTENTIAL_FIXED_MESSAGE.formatted(highest));
    }
    return true;
  }

  private static boolean hasMissingValue(PlayerCreateRequest request) {
    return Stream.of(
            request.pace(),
            request.shooting(),
            request.passing(),
            request.dribbling(),
            request.defending(),
            request.physical(),
            request.potential())
        .anyMatch(value -> value == null);
  }

  private static boolean reject(ConstraintValidatorContext context, String message) {
    context.disableDefaultConstraintViolation();
    context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    return false;
  }
}
