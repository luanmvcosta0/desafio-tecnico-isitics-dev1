package com.dev1_isitics.ligahub_be.modules.league.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.dev1_isitics.ligahub_be.modules.clubs.domain.Club;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class StandingsCalculatorTest {

  private static Club club(int id, String name, String acronym) {
    Club club = Club.found(name, acronym);
    ReflectionTestUtils.setField(club, "id", id);
    return club;
  }

  private static Match match(Club home, int homeGoals, Club away, int awayGoals) {
    return Match.record(
        new TeamSheet(home.getId(), Formation.TWO_ONE_ONE, List.of()),
        new TeamSheet(away.getId(), Formation.TWO_ONE_ONE, List.of()),
        homeGoals,
        awayGoals,
        Instant.EPOCH);
  }

  @Test
  void followsTheExampleOfTheStatement() {
    Club aguias = club(1, "Águias FC", "AGU");
    Club delta = club(2, "Delta FC", "DFC");
    Club gama = club(3, "Gama SC", "GSC");
    Club epsilon = club(4, "Épsilon EC", "EEC");
    Club mangue = club(5, "Mangue FC", "MFC");
    Club tubaroes = club(6, "Tubarões EC", "TEC");

    List<Standing> standings =
        StandingsCalculator.compute(
            List.of(aguias, delta, gama, epsilon, mangue, tubaroes),
            List.of(
                match(aguias, 1, delta, 2),
                match(gama, 2, epsilon, 1),
                match(epsilon, 3, aguias, 2),
                match(delta, 0, gama, 1)));

    assertThat(standings)
        .extracting(
            Standing::position,
            standing -> standing.club().getName(),
            Standing::points,
            Standing::goalDifference,
            Standing::goalsFor)
        .containsExactly(
            tuple(1, "Gama SC", 6, 2, 3),
            tuple(2, "Épsilon EC", 3, 0, 4),
            tuple(3, "Delta FC", 3, 0, 2),
            tuple(4, "Mangue FC", 0, 0, 0),
            tuple(5, "Tubarões EC", 0, 0, 0),
            tuple(6, "Águias FC", 0, -2, 3));
    assertThat(standings.get(0))
        .extracting(Standing::played, Standing::wins, Standing::draws, Standing::losses)
        .containsExactly(2, 2, 0, 0);
  }

  @Test
  void breaksFullTiesByAlphabeticalNameThenId() {
    Club second = club(2, "Aguias FC", "AFC");
    Club first = club(1, "Águias FC", "AGU");
    Club zeta = club(3, "Zeta FC", "ZFC");

    assertThat(StandingsCalculator.compute(List.of(zeta, second, first), List.of()))
        .extracting(standing -> standing.club().getId())
        .containsExactly(1, 2, 3);
  }
}
