package com.dev1_isitics.ligahub_be.modules.players.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Overall (RN06): attributes weighted by the position, divided by 100, rounded half up. */
public final class OverallCalculator {

  private static final BigDecimal TOTAL_WEIGHT = BigDecimal.valueOf(100);

  private OverallCalculator() {}

  public static int calculate(Position position, TechnicalAttributes attributes) {
    AttributeWeights weights = position.weights();
    int weightedSum =
        weights.pace() * attributes.getPace()
            + weights.shooting() * attributes.getShooting()
            + weights.passing() * attributes.getPassing()
            + weights.dribbling() * attributes.getDribbling()
            + weights.defending() * attributes.getDefending()
            + weights.physical() * attributes.getPhysical();
    return BigDecimal.valueOf(weightedSum)
        .divide(TOTAL_WEIGHT, 0, RoundingMode.HALF_UP)
        .intValueExact();
  }
}
