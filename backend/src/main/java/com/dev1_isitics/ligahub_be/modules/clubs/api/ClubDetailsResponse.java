package com.dev1_isitics.ligahub_be.modules.clubs.api;

import com.dev1_isitics.ligahub_be.modules.clubs.application.ClubDetails;
import java.math.BigDecimal;
import java.util.List;

/** {@code GET /clubs/{id}}: the club plus its squad and contracts in the squad order (RN11). */
public record ClubDetailsResponse(
    Integer id,
    String name,
    String acronym,
    BigDecimal cash,
    BigDecimal squadValue,
    BigDecimal netWorth,
    List<SquadPlayerResponse> players) {

  public static ClubDetailsResponse from(ClubDetails details) {
    ClubResponse club = ClubResponse.from(details.valuation());
    return new ClubDetailsResponse(
        club.id(),
        club.name(),
        club.acronym(),
        club.cash(),
        club.squadValue(),
        club.netWorth(),
        details.squad().stream().map(SquadPlayerResponse::from).toList());
  }
}
