package com.dev1_isitics.ligahub_be.modules.league.application;

import com.dev1_isitics.ligahub_be.modules.league.domain.Formation;
import com.dev1_isitics.ligahub_be.modules.players.domain.Player;
import java.util.List;

/**
 * Current lineup of a club (RN19): the saved formation and the starters that remain, in the squad
 * order. It is complete only with the five starters.
 */
public record Lineup(Integer clubId, Formation formation, List<Player> starters) {

  public boolean isComplete() {
    return formation != null && starters.size() == Formation.STARTERS;
  }
}
