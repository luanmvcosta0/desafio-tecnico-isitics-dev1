package com.dev1_isitics.ligahub_be.modules.market.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PurchaseFeeCalculatorTest {

  @ParameterizedTest(name = "cash {0}, value {1} -> fee {2}, accepted {3}")
  @CsvSource({
    "300.00, 28.00, 0.00, true",
    "300.00, 30.00, 0.00, true",
    "300.00, 30.01, 1.50, true",
    "100.00, 47.62, 2.38, true",
    "100.00, 48.00, 2.40, false"
  })
  void followsTheTableOfTheStatement(
      BigDecimal cash, BigDecimal value, BigDecimal expectedFee, boolean accepted) {
    BigDecimal fee = PurchaseFeeCalculator.fee(value);

    assertThat(fee).isEqualTo(expectedFee);
    assertThat(PurchaseFeeCalculator.isAffordable(value.add(fee), cash)).isEqualTo(accepted);
  }

  @Test
  void usesExactlyHalfOfTheCashWithoutRounding() {
    BigDecimal cash = new BigDecimal("261.15");

    assertThat(PurchaseFeeCalculator.cashLimit(cash)).isEqualByComparingTo("130.575");
    assertThat(PurchaseFeeCalculator.isAffordable(new BigDecimal("130.57"), cash)).isTrue();
    assertThat(PurchaseFeeCalculator.isAffordable(new BigDecimal("130.58"), cash)).isFalse();
  }
}
