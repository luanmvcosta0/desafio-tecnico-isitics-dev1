package com.dev1_isitics.ligahub_be.shared.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Coins always have 2 decimal places, rounded half away from zero only on the final result. */
public final class Money {

  public static final int SCALE = 2;
  public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
  public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(SCALE);

  private Money() {}

  public static BigDecimal round(BigDecimal value) {
    return value.setScale(SCALE, ROUNDING);
  }

  /** Value with decimal comma for error messages, e.g. "130,575" or "50,40". */
  public static String display(BigDecimal value) {
    BigDecimal shown = value.scale() < SCALE ? value.setScale(SCALE) : value;
    return shown.toPlainString().replace('.', ',');
  }
}
