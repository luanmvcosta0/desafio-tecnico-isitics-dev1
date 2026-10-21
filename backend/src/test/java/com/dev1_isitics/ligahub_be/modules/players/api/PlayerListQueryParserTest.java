package com.dev1_isitics.ligahub_be.modules.players.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dev1_isitics.ligahub_be.modules.players.application.PlayerListQuery;
import com.dev1_isitics.ligahub_be.modules.players.application.PlayerSort;
import com.dev1_isitics.ligahub_be.modules.players.domain.Condition;
import com.dev1_isitics.ligahub_be.modules.players.domain.Position;
import com.dev1_isitics.ligahub_be.shared.exception.InvalidRequestException;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class PlayerListQueryParserTest {

  @Test
  void usesDefaultsWhenNothingIsInformed() {
    PlayerListQuery query = PlayerListQueryParser.parse(Map.of("unknown", "x"));

    assertThat(query.sorts()).containsExactly(PlayerSort.NAME);
    assertThat(query.page()).isEqualTo(1);
    assertThat(query.limit()).isEqualTo(10);
    assertThat(query.minAge()).isEqualTo(16);
    assertThat(query.maxAge()).isEqualTo(45);
    assertThat(query.minValue()).isEqualByComparingTo("0");
    assertThat(query.maxValue()).isEqualByComparingTo("550");
    assertThat(query.search()).isNull();
  }

  @Test
  void readsEveryFilter() {
    PlayerListQuery query =
        PlayerListQueryParser.parse(
            Map.ofEntries(
                Map.entry("search", "  meia   lua "),
                Map.entry("position", "MEI"),
                Map.entry("condition", "MAL_CONDICIONADO"),
                Map.entry("free", "false"),
                Map.entry("club_id", "3"),
                Map.entry("min_age", "20"),
                Map.entry("max_age", "30"),
                Map.entry("min_value", "0.5"),
                Map.entry("max_value", "10.55"),
                Map.entry("sort", "value_desc,age_asc"),
                Map.entry("page", "2"),
                Map.entry("limit", "50")));

    assertThat(query.search()).isEqualTo("meia lua");
    assertThat(query.position()).isEqualTo(Position.MEI);
    assertThat(query.condition()).isEqualTo(Condition.MAL_CONDICIONADO);
    assertThat(query.free()).isFalse();
    assertThat(query.clubId()).isEqualTo(3);
    assertThat(query.minValue()).isEqualTo(new BigDecimal("0.5"));
    assertThat(query.sorts()).containsExactly(PlayerSort.VALUE_DESC, PlayerSort.AGE_ASC);
    assertThat(query.page()).isEqualTo(2);
  }

  @Test
  void keepsTheOtherExtremeWhenOnlyOneLimitIsInformed() {
    PlayerListQuery query = PlayerListQueryParser.parse(Map.of("min_age", "40"));

    assertThat(query.maxAge()).isEqualTo(45);
  }

  @ParameterizedTest(name = "{0}={1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "position|''",
        "position|mei",
        "condition|apto",
        "free|TRUE",
        "free|1",
        "club_id|0",
        "club_id|-1",
        "club_id|01",
        "club_id|1.0",
        "club_id|1e2",
        "min_age|15",
        "max_age|46",
        "min_value|-1",
        "min_value|550.01",
        "min_value|01",
        "min_value|1.555",
        "min_value|1,5",
        "sort|POSITION",
        "sort|name,",
        "sort|name, age_asc",
        "sort|value_asc,value_desc",
        "sort|name,age_asc,position",
        "page|0",
        "limit|51",
        "limit|0",
        "search|a",
        "search|'   '",
        "search|'meia\tlua'"
      })
  void rejectsInvalidValues(String name, String value) {
    assertThatThrownBy(() -> PlayerListQueryParser.parse(Map.of(name, value)))
        .isInstanceOf(InvalidRequestException.class);
  }

  @ParameterizedTest
  @CsvSource({"free, true, club_id, 1", "min_age, 30, max_age, 20", "min_value, 10, max_value, 9.99"})
  void rejectsContradictoryFilters(String first, String firstValue, String second, String value) {
    assertThatThrownBy(() -> PlayerListQueryParser.parse(Map.of(first, firstValue, second, value)))
        .isInstanceOf(InvalidRequestException.class);
  }

  @Test
  void acceptsOnlyClubPlayersWithClubFilter() {
    PlayerListQuery query = PlayerListQueryParser.parse(Map.of("free", "false", "club_id", "1"));

    assertThat(query.clubId()).isEqualTo(1);
  }
}
