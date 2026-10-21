package com.dev1_isitics.ligahub_be.modules.players.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class MarketValueCalculatorTest {

  @ParameterizedTest(name = "{0}, {1}, agility {2}, age {3} -> {4}")
  @CsvSource({
    "82.00, APTO, 20, 25, 82.00",
    "82.00, APTO, 50, 19, 90.20",
    "82.00, MAL_CONDICIONADO, 50, 25, 69.70",
    "82.00, MAL_CONDICIONADO, 80, 21, 84.05",
    "82.00, MAL_CONDICIONADO, 30, 33, 53.30",
    "82.00, DOENTE, 90, 25, 69.70",
    "82.00, DOENTE, 30, 25, 49.20",
    "82.00, DOENTE, 10, 40, 49.20",
    "9.50, MAL_CONDICIONADO, 85, 25, 8.79",
    "1.00, MAL_CONDICIONADO, 50, 35, 1.00"
  })
  void followsTheExamplesOfTheStatement(
      BigDecimal baseValue, Condition condition, int agility, int age, BigDecimal expected) {
    assertThat(MarketValueCalculator.calculate(baseValue, condition, agility, age))
        .isEqualTo(expected);
  }
}
