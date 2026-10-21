package com.dev1_isitics.ligahub_be.modules.clubs.api;

import com.dev1_isitics.ligahub_be.modules.clubs.domain.ClubValuation;
import java.math.BigDecimal;

/** Club of {@code POST /clubs} and of the items of {@code GET /clubs}, without the squad. */
public record ClubResponse(
    Integer id,
    String name,
    String acronym,
    BigDecimal cash,
    BigDecimal squadValue,
    BigDecimal netWorth) {

  public static ClubResponse from(ClubValuation valuation) {
    return new ClubResponse(
        valuation.club().getId(),
        valuation.club().getName(),
        valuation.club().getAcronym(),
        valuation.club().getCash(),
        valuation.squadValue(),
        valuation.netWorth());
  }
}
