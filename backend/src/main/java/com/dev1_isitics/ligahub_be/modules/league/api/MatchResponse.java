package com.dev1_isitics.ligahub_be.modules.league.api;

import com.dev1_isitics.ligahub_be.modules.clubs.domain.Club;
import com.dev1_isitics.ligahub_be.modules.league.application.PlayedMatch;
import com.dev1_isitics.ligahub_be.modules.league.domain.Formation;
import com.dev1_isitics.ligahub_be.modules.league.domain.MatchDuel;
import com.dev1_isitics.ligahub_be.modules.league.domain.MatchResult;
import com.dev1_isitics.ligahub_be.modules.players.domain.Condition;
import com.dev1_isitics.ligahub_be.modules.players.domain.Position;
import com.dev1_isitics.ligahub_be.shared.time.BrasiliaDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * A match of {@code POST /matches} and {@code GET /matches/{id}}, with the six duels; the items of
 * {@code GET /matches} have the same format without {@code duels}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record MatchResponse(
    Integer id,
    String playedAt,
    Side home,
    Side away,
    MatchResult result,
    List<Duel> duels) {

  public record Side(Integer clubId, String clubName, Formation formation, int goals) {

    static Side of(Club club, Formation formation, int goals) {
      return new Side(club.getId(), club.getName(), formation, goals);
    }
  }

  public record DuelPlayer(
      Integer playerId,
      String playerName,
      Position position,
      Condition condition,
      String attribute,
      int value) {}

  public record Duel(
      int order, Integer attackingClubId, DuelPlayer attacker, DuelPlayer defender, boolean goal) {

    static Duel from(MatchDuel duel) {
      return new Duel(
          duel.getOrder(),
          duel.getAttackingClubId(),
          new DuelPlayer(
              duel.getAttackerId(),
              duel.getAttackerName(),
              duel.getAttackerPosition(),
              duel.getAttackerCondition(),
              duel.getAttackAttribute(),
              duel.getAttackValue()),
          new DuelPlayer(
              duel.getDefenderId(),
              duel.getDefenderName(),
              duel.getDefenderPosition(),
              duel.getDefenderCondition(),
              duel.getDefenseAttribute(),
              duel.getDefenseValue()),
          duel.isGoal());
    }
  }

  public static MatchResponse withDuels(PlayedMatch played) {
    return of(played, played.duels().stream().map(Duel::from).toList());
  }

  public static MatchResponse withoutDuels(PlayedMatch played) {
    return of(played, null);
  }

  private static MatchResponse of(PlayedMatch played, List<Duel> duels) {
    return new MatchResponse(
        played.match().getId(),
        BrasiliaDateTime.format(played.match().getPlayedAt()),
        Side.of(played.home(), played.match().getHomeFormation(), played.match().getHomeGoals()),
        Side.of(played.away(), played.match().getAwayFormation(), played.match().getAwayGoals()),
        played.match().result(),
        duels);
  }
}
