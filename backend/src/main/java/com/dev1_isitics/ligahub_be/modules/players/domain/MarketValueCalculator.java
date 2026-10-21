package com.dev1_isitics.ligahub_be.modules.players.domain;

import com.dev1_isitics.ligahub_be.shared.money.Money;
import java.math.BigDecimal;

/**
 * Market value (RN02–RN05), computed on every read: condition discount adjusted by agility, age
 * adjustment, total capped at -40%, rounded once and never below 1,00.
 */
public final class MarketValueCalculator {

  private static final int HIGH_AGILITY_MIN = 80;
  private static final int LOW_AGILITY_MAX = 30;
  private static final BigDecimal HIGH_AGILITY_DISCOUNT_DIVISOR = BigDecimal.valueOf(2);
  private static final BigDecimal LOW_AGILITY_EXTRA_DISCOUNT = BigDecimal.valueOf(10);

  private static final int YOUNG_MAX_AGE = 21;
  private static final int VETERAN_MIN_AGE = 32;
  private static final BigDecimal YOUNG_ADJUSTMENT = BigDecimal.valueOf(10);
  private static final BigDecimal VETERAN_ADJUSTMENT = BigDecimal.valueOf(-10);

  private static final BigDecimal MAX_TOTAL_DISCOUNT = BigDecimal.valueOf(-40);
  private static final BigDecimal HUNDRED_PERCENT = BigDecimal.valueOf(100);
  private static final BigDecimal MINIMUM_VALUE = new BigDecimal("1.00");

  private MarketValueCalculator() {}

  public static BigDecimal calculate(
      BigDecimal baseValue, Condition condition, int agility, int age) {
    BigDecimal totalPercent =
        ageAdjustment(age).subtract(conditionDiscount(condition, agility)).max(MAX_TOTAL_DISCOUNT);
    BigDecimal value = baseValue.multiply(HUNDRED_PERCENT.add(totalPercent)).movePointLeft(2);
    return Money.round(value).max(MINIMUM_VALUE);
  }

  private static BigDecimal conditionDiscount(Condition condition, int agility) {
    BigDecimal discount = condition.discountPercent();
    if (condition == Condition.APTO) {
      return discount;
    }
    if (agility >= HIGH_AGILITY_MIN) {
      return discount.divide(HIGH_AGILITY_DISCOUNT_DIVISOR);
    }
    if (agility <= LOW_AGILITY_MAX) {
      return discount.add(LOW_AGILITY_EXTRA_DISCOUNT);
    }
    return discount;
  }

  private static BigDecimal ageAdjustment(int age) {
    if (age <= YOUNG_MAX_AGE) {
      return YOUNG_ADJUSTMENT;
    }
    if (age >= VETERAN_MIN_AGE) {
      return VETERAN_ADJUSTMENT;
    }
    return BigDecimal.ZERO;
  }
}
