package com.dev1_isitics.ligahub_be.shared.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Coins always have 2 decimal places, rounded half away from zero only on the final result. */
public final class Money {

  public static final int SCALE = 2;
  public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

  private Money() {}

  public static BigDecimal round(BigDecimal value) {
    return value.setScale(SCALE, ROUNDING);
  }
}
