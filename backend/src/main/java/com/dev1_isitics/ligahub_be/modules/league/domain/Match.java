package com.dev1_isitics.ligahub_be.modules.league.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** A played match with the score and the formations of the moment; never recalculated. */
@Entity
@Table(name = "partidas")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Match {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "mandante_id", nullable = false)
  private Integer homeClubId;

  @Column(name = "visitante_id", nullable = false)
  private Integer awayClubId;

  @Column(name = "gols_mandante", nullable = false)
  private int homeGoals;

  @Column(name = "gols_visitante", nullable = false)
  private int awayGoals;

  @Convert(converter = FormationConverter.class)
  @Column(name = "formacao_mandante", nullable = false, length = 5)
  private Formation homeFormation;

  @Convert(converter = FormationConverter.class)
  @Column(name = "formacao_visitante", nullable = false, length = 5)
  private Formation awayFormation;

  @Column(name = "jogada_em", nullable = false)
  private Instant playedAt;

  public static Match record(
      TeamSheet home, TeamSheet away, int homeGoals, int awayGoals, Instant playedAt) {
    Match match = new Match();
    match.homeClubId = home.clubId();
    match.awayClubId = away.clubId();
    match.homeFormation = home.formation();
    match.awayFormation = away.formation();
    match.homeGoals = homeGoals;
    match.awayGoals = awayGoals;
    match.playedAt = playedAt;
    return match;
  }

  public MatchResult result() {
    return MatchResult.of(homeGoals, awayGoals);
  }
}
