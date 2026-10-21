package com.dev1_isitics.ligahub_be.modules.league.api;

import com.dev1_isitics.ligahub_be.modules.league.application.Lineup;
import com.dev1_isitics.ligahub_be.modules.league.domain.Formation;
import com.dev1_isitics.ligahub_be.modules.players.api.PlayerResponse;
import java.util.List;

/** {@code GET} and {@code PUT /clubs/{id}/lineup}; without a lineup, formation is null (RN19). */
public record LineupResponse(
    Integer clubId, Formation formation, boolean complete, List<PlayerResponse> players) {

  public static LineupResponse from(Lineup lineup) {
    return new LineupResponse(
        lineup.clubId(),
        lineup.formation(),
        lineup.isComplete(),
        lineup.starters().stream().map(PlayerResponse::from).toList());
  }
}
