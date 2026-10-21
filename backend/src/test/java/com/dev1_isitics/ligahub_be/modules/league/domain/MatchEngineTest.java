package com.dev1_isitics.ligahub_be.modules.league.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.dev1_isitics.ligahub_be.modules.players.domain.Condition;
import com.dev1_isitics.ligahub_be.modules.players.domain.Player;
import com.dev1_isitics.ligahub_be.modules.players.domain.Position;
import com.dev1_isitics.ligahub_be.modules.players.domain.TechnicalAttributes;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.test.util.ReflectionTestUtils;

class MatchEngineTest {

  private static final int AGUIAS = 1;
  private static final int DELTA = 2;
  private static int nextId = 1;

  private static Player player(
      String name, Position position, int dribbling, int passing, int shooting, int defending) {
    TechnicalAttributes attributes =
        new TechnicalAttributes(50, shooting, passing, dribbling, defending, 50);
    Player player = Player.register(name, position, 50, 30, attributes, 99);
    ReflectionTestUtils.setField(player, "id", nextId++);
    return player;
  }

  private static Player unfit(Player player) {
    player.changeCondition(Condition.MAL_CONDICIONADO);
    return player;
  }

  private static TeamSheet aguias() {
    return new TeamSheet(
        AGUIAS,
        Formation.TWO_ONE_ONE,
        List.of(
            player("Rui Prado", Position.GOL, 50, 50, 50, 77),
            player("Téo Brandão", Position.ZAG, 50, 50, 50, 84),
            unfit(player("Davi Lins", Position.LAT, 74, 50, 50, 66)),
            player("Lauro Neves", Position.MEI, 50, 82, 50, 50),
            player("Otto Rangel", Position.ATA, 50, 50, 75, 50)));
  }

  private static TeamSheet delta() {
    return new TeamSheet(
        DELTA,
        Formation.TWO_ONE_ONE,
        List.of(
            player("Ivo Teles", Position.GOL, 50, 50, 50, 72),
            player("Hugo Vaz", Position.ZAG, 50, 50, 50, 84),
            player("Lino Matos", Position.LAT, 79, 50, 50, 63),
            player("Nei Soares", Position.MEI, 50, 70, 50, 50),
            player("Ugo Falcão", Position.ATA, 50, 50, 81, 50)));
  }

  @Test
  void playsTheCompleteExampleOfTheStatement() {
    List<DuelResult> duels = MatchEngine.play(aguias(), delta());

    assertThat(duels)
        .extracting(
            DuelResult::order,
            duel -> duel.attacker().getName(),
            DuelResult::attackAttribute,
            DuelResult::attackValue,
            duel -> duel.defender().getName(),
            DuelResult::defenseValue,
            DuelResult::isGoal)
        .containsExactly(
            tuple(1, "Otto Rangel", DuelAttribute.SHOOTING, 75, "Ivo Teles", 72, true),
            tuple(2, "Lauro Neves", DuelAttribute.PASSING, 82, "Hugo Vaz", 84, false),
            tuple(3, "Davi Lins", DuelAttribute.DRIBBLING, 63, "Lino Matos", 63, false),
            tuple(4, "Ugo Falcão", DuelAttribute.SHOOTING, 81, "Rui Prado", 77, true),
            tuple(5, "Nei Soares", DuelAttribute.PASSING, 70, "Téo Brandão", 84, false),
            tuple(6, "Lino Matos", DuelAttribute.DRIBBLING, 79, "Davi Lins", 56, true));
    assertThat(duels).extracting(DuelResult::attackingClubId).containsExactly(1, 1, 1, 2, 2, 2);
    assertThat(duels).allMatch(duel -> duel.defenseAttribute() == DuelAttribute.DEFENDING);
    assertThat(MatchEngine.goalsOf(AGUIAS, duels)).isEqualTo(1);
    assertThat(MatchEngine.goalsOf(DELTA, duels)).isEqualTo(2);
    assertThat(MatchResult.of(1, 2)).isEqualTo(MatchResult.VISITANTE);
  }

  @ParameterizedTest(name = "{0} × 0,85 -> {1}")
  @CsvSource({"74, 63", "50, 43", "66, 56", "1, 1", "99, 84"})
  void roundsTheUnfitAttributesHalfUp(int attribute, int expected) {
    TeamSheet home = aguias();
    Player attacker = unfit(player("Otto Rangel", Position.ATA, 50, 50, attribute, 50));
    TeamSheet unfitHome =
        new TeamSheet(
            AGUIAS,
            Formation.TWO_ONE_ONE,
            List.of(
                home.goalkeeper(), home.outfield(1), home.outfield(2), home.outfield(3), attacker));

    assertThat(MatchEngine.play(unfitHome, delta()).get(0).attackValue()).isEqualTo(expected);
  }

  @ParameterizedTest
  @CsvSource({"2, 1, MANDANTE", "0, 3, VISITANTE", "1, 1, EMPATE", "0, 0, EMPATE"})
  void derivesTheResultFromTheScore(int home, int away, MatchResult expected) {
    assertThat(MatchResult.of(home, away)).isEqualTo(expected);
  }
}
