package com.dev1_isitics.ligahub_be.modules.league.domain;

import com.dev1_isitics.ligahub_be.modules.players.domain.Player;

/** One of the six duels, with the players and the values actually used (RN20). */
public record DuelResult(
    int order,
    Integer attackingClubId,
    Player attacker,
    DuelAttribute attackAttribute,
    int attackValue,
    Player defender,
    DuelAttribute defenseAttribute,
    int defenseValue) {

  /** A goal only when the attack is greater than the defense; a tie is not a goal. */
  public boolean isGoal() {
    return attackValue > defenseValue;
  }
}
