package com.dev1_isitics.ligahub_be.modules.league.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dev1_isitics.ligahub_be.modules.players.domain.Position;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class FormationTest {

  private static List<Position> positions(String names) {
    return Arrays.stream(names.split(" ")).map(Position::valueOf).toList();
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({
    "3-1, GOL ZAG LAT LAT ATA",
    "3-1, ATA ZAG ZAG ZAG GOL",
    "2-2, GOL ZAG LAT ATA ATA",
    "2-1-1, GOL ZAG LAT MEI ATA",
    "1-2-1, GOL ZAG MEI MEI ATA",
    "1-2-1, GOL LAT MEI MEI ATA"
  })
  void isClosedByTheSectorsOfTheStatement(String code, String starters) {
    assertThat(Formation.fromCode(code).isClosedBy(positions(starters))).isTrue();
  }

  @ParameterizedTest(name = "{0}: {1}")
  @CsvSource({
    "3-1, GOL ZAG LAT MEI ATA",
    "2-2, GOL ZAG LAT MEI ATA",
    "2-1-1, GOL GOL LAT MEI ATA",
    "2-1-1, ZAG ZAG LAT MEI ATA",
    "1-2-1, GOL ZAG MEI ATA ATA",
    "2-1-1, GOL ZAG LAT MEI"
  })
  void isNotClosedByOtherPositions(String code, String starters) {
    assertThat(Formation.fromCode(code).isClosedBy(positions(starters))).isFalse();
  }

  @ParameterizedTest
  @ValueSource(strings = {"2-1-1 ", "4-0", "211", "3_1", ""})
  void acceptsOnlyTheExactCodes(String code) {
    assertThatThrownBy(() -> Formation.fromCode(code)).isInstanceOf(IllegalArgumentException.class);
  }
}
