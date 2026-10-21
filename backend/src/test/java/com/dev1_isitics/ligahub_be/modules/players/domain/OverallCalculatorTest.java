package com.dev1_isitics.ligahub_be.modules.players.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class OverallCalculatorTest {

  @ParameterizedTest(name = "{0} ({1}, {2}, {3}, {4}, {5}, {6}) -> {7}")
  @CsvSource({
    "MEI, 68, 65, 80, 78, 52, 60, 73",
    "ATA, 78, 75, 73, 82, 48, 68, 76",
    "LAT, 72, 70, 78, 76, 70, 66, 72",
    "ZAG, 50, 90, 60, 40, 80, 75, 73",
    "GOL, 55, 30, 65, 40, 72, 68, 68"
  })
  void weightsAttributesByPositionAndRoundsHalfUp(
      Position position,
      int pace,
      int shooting,
      int passing,
      int dribbling,
      int defending,
      int physical,
      int expected) {
    TechnicalAttributes attributes =
        new TechnicalAttributes(pace, shooting, passing, dribbling, defending, physical);

    assertThat(OverallCalculator.calculate(position, attributes)).isEqualTo(expected);
  }
}
