package com.dev1_isitics.ligahub_be.modules.market.api;

import com.dev1_isitics.ligahub_be.shared.time.BrasiliaDateTime;
import com.dev1_isitics.ligahub_be.shared.validation.BrasiliaDateTimeFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.Instant;

/**
 * Body of {@code POST /players/{id}/buy}. The loyalty end arrives as text so that its format is
 * validated by the RN01 rule, not by a lenient date parser.
 */
public record PurchaseRequest(
    @NotNull @Positive Integer clubId, @NotNull @BrasiliaDateTimeFormat String loyaltyUntil) {

  public Instant loyaltyUntilInstant() {
    return BrasiliaDateTime.parseRequest(loyaltyUntil).orElseThrow();
  }
}
