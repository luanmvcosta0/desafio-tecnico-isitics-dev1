package com.dev1_isitics.ligahub_be.modules.market.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Loyalty penalty (RN15): remaining days until the end of the loyalty, with fraction and rounded
 * up (0 once it is over), and the penalty rate of its bracket.
 */
public final class LoyaltyPenaltyCalculator {

  private static final long NANOS_PER_DAY = Duration.ofDays(1).toNanos();
  private static final List<PenaltyBracket> BRACKETS =
      List.of(new PenaltyBracket(181, 20), new PenaltyBracket(31, 10), new PenaltyBracket(1, 5));
  private static final int NO_PENALTY = 0;

  private LoyaltyPenaltyCalculator() {}

  public static long remainingDays(Instant loyaltyUntil, Instant now) {
    Duration remaining = Duration.between(now, loyaltyUntil);
    if (remaining.isNegative() || remaining.isZero()) {
      return 0;
    }
    return Math.ceilDiv(remaining.toNanos(), NANOS_PER_DAY);
  }

  /** Percentage: 0 days → 0%; 1–30 → 5%; 31–180 → 10%; 181 or more → 20%. */
  public static int penaltyRate(long remainingDays) {
    return BRACKETS.stream()
        .filter(bracket -> remainingDays >= bracket.minDays())
        .map(PenaltyBracket::rate)
        .findFirst()
        .orElse(NO_PENALTY);
  }

  private record PenaltyBracket(long minDays, int rate) {}
}
