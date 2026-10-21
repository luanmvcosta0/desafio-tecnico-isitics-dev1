package com.dev1_isitics.ligahub_be.modules.league.domain;

/** Result of a match, derived from the score (RN20). */
public enum MatchResult {
  MANDANTE,
  VISITANTE,
  EMPATE;

  public static MatchResult of(int homeGoals, int awayGoals) {
    if (homeGoals > awayGoals) {
      return MANDANTE;
    }
    return homeGoals < awayGoals ? VISITANTE : EMPATE;
  }
}
