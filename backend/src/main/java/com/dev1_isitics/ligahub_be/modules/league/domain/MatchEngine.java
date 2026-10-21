package com.dev1_isitics.ligahub_be.modules.league.domain;

import com.dev1_isitics.ligahub_be.modules.players.domain.Condition;
import com.dev1_isitics.ligahub_be.modules.players.domain.Player;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Plays a match (RN20). Each team makes 3 attacks: its 4th, 3rd and 2nd outfield players attack the
 * goalkeeper, the 1st and the 2nd outfield players of the opponent. Duels 1–3 are the home attacks
 * and 4–6 the away attacks. MAL_CONDICIONADO players use each attribute × 0,85, rounded half up.
 */
public final class MatchEngine {

  private static final BigDecimal UNFIT_FACTOR = new BigDecimal("0.85");
  private static final int[] ATTACKERS = {4, 3, 2};
  private static final int[] DEFENDERS = {0, 1, 2};
  private static final int GOALKEEPER = 0;

  private MatchEngine() {}

  public static List<DuelResult> play(TeamSheet home, TeamSheet away) {
    List<DuelResult> duels = new ArrayList<>();
    attack(home, away, duels);
    attack(away, home, duels);
    return duels;
  }

  public static int goalsOf(Integer clubId, List<DuelResult> duels) {
    return (int)
        duels.stream()
            .filter(duel -> duel.attackingClubId().equals(clubId) && duel.isGoal())
            .count();
  }

  private static void attack(TeamSheet attacking, TeamSheet defending, List<DuelResult> duels) {
    for (int index = 0; index < ATTACKERS.length; index++) {
      Player attacker = attacking.outfield(ATTACKERS[index]);
      Player defender =
          DEFENDERS[index] == GOALKEEPER
              ? defending.goalkeeper()
              : defending.outfield(DEFENDERS[index]);
      DuelAttribute attackAttribute = DuelAttribute.ofAttacker(attacker.getPosition());
      duels.add(
          new DuelResult(
              duels.size() + 1,
              attacking.clubId(),
              attacker,
              attackAttribute,
              valueOf(attacker, attackAttribute),
              defender,
              DuelAttribute.DEFENDING,
              valueOf(defender, DuelAttribute.DEFENDING)));
    }
  }

  private static int valueOf(Player player, DuelAttribute attribute) {
    int value = attribute.read(player.getAttributes());
    if (player.getCondition() != Condition.MAL_CONDICIONADO) {
      return value;
    }
    return BigDecimal.valueOf(value)
        .multiply(UNFIT_FACTOR)
        .setScale(0, RoundingMode.HALF_UP)
        .intValueExact();
  }
}
