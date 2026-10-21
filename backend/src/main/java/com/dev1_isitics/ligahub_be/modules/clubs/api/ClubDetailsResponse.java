package com.dev1_isitics.ligahub_be.modules.clubs.api;

import com.dev1_isitics.ligahub_be.modules.clubs.domain.ClubValuation;
import com.dev1_isitics.ligahub_be.modules.players.api.PlayerResponse;
import java.math.BigDecimal;
import java.util.List;

/** {@code GET /clubs/{id}}: the club plus its squad in the squad order (RN11). */
public record ClubDetailsResponse(
    Integer id,
    String name,
    String acronym,
    BigDecimal cash,
    BigDecimal squadValue,
    BigDecimal netWorth,
    List<PlayerResponse> players) {

  public static ClubDetailsResponse from(ClubValuation valuation) {
    ClubResponse club = ClubResponse.from(valuation);
    return new ClubDetailsResponse(
        club.id(),
        club.name(),
        club.acronym(),
        club.cash(),
        club.squadValue(),
        club.netWorth(),
        valuation.squad().stream().map(PlayerResponse::from).toList());
  }
}
