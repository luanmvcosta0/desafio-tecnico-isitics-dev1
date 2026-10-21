package com.dev1_isitics.ligahub_be.modules.players.domain;

import com.dev1_isitics.ligahub_be.shared.money.Money;
import java.math.BigDecimal;
import java.util.List;

/**
 * Base value (RN06), computed once at registration: a progressive amount per overall point, raised
 * by the potential for young prospects, rounded once and never below 1,00.
 */
public final class BaseValueCalculator {

  private static final List<OverallTier> TIERS =
      List.of(
          new OverallTier(41, 60, new BigDecimal("0.50")),
          new OverallTier(61, 75, new BigDecimal("2.00")),
          new OverallTier(76, 99, new BigDecimal("6.00")));
  private static final int YOUNG_PROSPECT_MAX_AGE = 21;
  private static final int PERCENT = 100;
  private static final BigDecimal MINIMUM_VALUE = new BigDecimal("1.00");

  private BaseValueCalculator() {}

  public static BigDecimal calculate(int overall, int potential, int age) {
    BigDecimal overallPart =
        TIERS.stream().map(tier -> tier.valueFor(overall)).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal value =
        age <= YOUNG_PROSPECT_MAX_AGE
            ? overallPart
                .multiply(BigDecimal.valueOf(PERCENT + potential - overall))
                .movePointLeft(2)
            : overallPart;
    return Money.round(value).max(MINIMUM_VALUE);
  }

  private record OverallTier(int firstPoint, int lastPoint, BigDecimal valuePerPoint) {

    BigDecimal valueFor(int overall) {
      int points = Math.max(0, Math.min(overall, lastPoint) - firstPoint + 1);
      return valuePerPoint.multiply(BigDecimal.valueOf(points));
    }
  }
}
