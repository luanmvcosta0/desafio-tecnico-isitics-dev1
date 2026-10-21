package com.dev1_isitics.ligahub_be.modules.league.domain;

import com.dev1_isitics.ligahub_be.modules.players.domain.Player;
import java.util.List;

/**
 * The five starters of a club for a match, in the squad order (RN20): the goalkeeper and the four
 * outfield players numbered from the 1st (furthest back) to the 4th (furthest forward).
 */
public record TeamSheet(Integer clubId, Formation formation, List<Player> starters) {

  private static final int GOALKEEPER_INDEX = 0;

  public Player goalkeeper() {
    return starters.get(GOALKEEPER_INDEX);
  }

  /** The n-th outfield player, n from 1 to 4. */
  public Player outfield(int number) {
    return starters.get(number);
  }
}
