package com.dev1_isitics.ligahub_be.modules.league.api;

import com.dev1_isitics.ligahub_be.modules.league.domain.Standing;

/** Row of {@code GET /standings}. */
public record StandingResponse(
    int position,
    Integer clubId,
    String clubName,
    int played,
    int wins,
    int draws,
    int losses,
    int goalsFor,
    int goalsAgainst,
    int goalDifference,
    int points) {

  public static StandingResponse from(Standing standing) {
    return new StandingResponse(
        standing.position(),
        standing.club().getId(),
        standing.club().getName(),
        standing.played(),
        standing.wins(),
        standing.draws(),
        standing.losses(),
        standing.goalsFor(),
        standing.goalsAgainst(),
        standing.goalDifference(),
        standing.points());
  }
}
