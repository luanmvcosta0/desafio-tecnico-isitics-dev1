package com.dev1_isitics.ligahub_be.modules.market.domain;

import com.dev1_isitics.ligahub_be.shared.money.Money;
import java.math.BigDecimal;

/**
 * Purchase fee and cash limit (RN13): 5% of the market value, charged only above 30,00, and the
 * total cost may be at most exactly half of the cash before the purchase (no rounding of the limit).
 */
public final class PurchaseFeeCalculator {

  private static final BigDecimal FEE_FREE_UP_TO = new BigDecimal("30.00");
  private static final BigDecimal FEE_RATE = new BigDecimal("0.05");
  private static final BigDecimal CASH_LIMIT_DIVISOR = BigDecimal.valueOf(2);

  private PurchaseFeeCalculator() {}

  public static BigDecimal fee(BigDecimal marketValue) {
    if (marketValue.compareTo(FEE_FREE_UP_TO) <= 0) {
      return Money.ZERO;
    }
    return Money.round(marketValue.multiply(FEE_RATE));
  }

  /** Half of the cash, exact: 261,15 gives 130,575. */
  public static BigDecimal cashLimit(BigDecimal cash) {
    return cash.divide(CASH_LIMIT_DIVISOR);
  }

  public static boolean isAffordable(BigDecimal totalCost, BigDecimal cash) {
    return totalCost.compareTo(cashLimit(cash)) <= 0;
  }
}
