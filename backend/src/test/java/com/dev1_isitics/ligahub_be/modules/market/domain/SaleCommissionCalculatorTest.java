package com.dev1_isitics.ligahub_be.modules.market.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SaleCommissionCalculatorTest {

  @ParameterizedTest(name = "profit {0} -> {1}")
  @CsvSource({
    "3.00, 0.30",
    "3.85, 0.39",
    "10.00, 1.00",
    "10.01, 1.00",
    "15.00, 2.00",
    "-7.50, 0.00",
    "0.00, 0.00"
  })
  void isProgressiveAndRoundedOnce(BigDecimal profit, BigDecimal expected) {
    assertThat(SaleCommissionCalculator.commission(profit)).isEqualTo(expected);
  }

  @ParameterizedTest(name = "bought {0}, sold {1}, penalty {2} -> credit {5}")
  @CsvSource({
    "37.00, 40.00, 4.00, 3.00, 0.30, 35.70",
    "50.00, 42.50, 8.50, -7.50, 0.00, 34.00",
    "26.15, 30.00, 1.50, 3.85, 0.39, 28.11",
    "25.00, 25.00, 0.00, 0.00, 0.00, 25.00",
    "60.00, 75.00, 10.00, 15.00, 2.00, 63.00"
  })
  void followsTheSaleTableOfTheStatement(
      BigDecimal purchase,
      BigDecimal sale,
      BigDecimal penalty,
      BigDecimal expectedProfit,
      BigDecimal expectedCommission,
      BigDecimal expectedCredit) {
    BigDecimal profit = SaleCommissionCalculator.profit(sale, purchase);
    BigDecimal commission = SaleCommissionCalculator.commission(profit);

    assertThat(profit).isEqualByComparingTo(expectedProfit);
    assertThat(commission).isEqualTo(expectedCommission);
    assertThat(sale.subtract(penalty).subtract(commission)).isEqualByComparingTo(expectedCredit);
  }
}
