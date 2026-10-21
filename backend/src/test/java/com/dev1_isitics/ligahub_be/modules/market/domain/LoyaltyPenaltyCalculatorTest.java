package com.dev1_isitics.ligahub_be.modules.market.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class LoyaltyPenaltyCalculatorTest {

  private static final Instant NOW = Instant.parse("2026-11-05T19:40:00Z");

  @ParameterizedTest(name = "{0} -> {1} days, {2}%")
  @CsvSource({
    "-PT1H, 0, 0",
    "PT0S, 0, 0",
    "P10DT2H, 11, 5",
    "P30D, 30, 5",
    "P30DT1M, 31, 10",
    "P180D, 180, 10",
    "P180DT1M, 181, 20",
    "P200D, 200, 20",
    "P116DT17H35M, 117, 10"
  })
  void roundsRemainingDaysUpAndPicksTheBracket(
      Duration untilLoyaltyEnd, long expectedDays, int expectedRate) {
    long remainingDays = LoyaltyPenaltyCalculator.remainingDays(NOW.plus(untilLoyaltyEnd), NOW);

    assertThat(remainingDays).isEqualTo(expectedDays);
    assertThat(LoyaltyPenaltyCalculator.penaltyRate(remainingDays)).isEqualTo(expectedRate);
  }
}
