package com.dev1_isitics.ligahub_be.modules.market.domain;

import com.dev1_isitics.ligahub_be.shared.money.Money;
import java.math.BigDecimal;

/**
 * Sale commission (RN16): progressive, only on profit — 10% of the part up to 10,00 plus 20% of the
 * part above it, summed and rounded once. No profit means no commission.
 */
public final class SaleCommissionCalculator {

  private static final BigDecimal FIRST_BRACKET_LIMIT = new BigDecimal("10.00");
  private static final BigDecimal FIRST_BRACKET_RATE = new BigDecimal("0.10");
  private static final BigDecimal SECOND_BRACKET_RATE = new BigDecimal("0.20");

  private SaleCommissionCalculator() {}

  public static BigDecimal profit(BigDecimal saleValue, BigDecimal purchaseValue) {
    return saleValue.subtract(purchaseValue);
  }

  public static BigDecimal commission(BigDecimal profit) {
    if (profit.signum() <= 0) {
      return Money.ZERO;
    }
    BigDecimal firstPart = profit.min(FIRST_BRACKET_LIMIT);
    BigDecimal secondPart = profit.subtract(firstPart);
    return Money.round(
        firstPart.multiply(FIRST_BRACKET_RATE).add(secondPart.multiply(SECOND_BRACKET_RATE)));
  }
}
