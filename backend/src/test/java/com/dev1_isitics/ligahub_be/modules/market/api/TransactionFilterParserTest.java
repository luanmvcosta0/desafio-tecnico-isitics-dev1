package com.dev1_isitics.ligahub_be.modules.market.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dev1_isitics.ligahub_be.modules.market.application.TransactionFilter;
import com.dev1_isitics.ligahub_be.modules.market.domain.TransactionType;
import com.dev1_isitics.ligahub_be.shared.exception.InvalidRequestException;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class TransactionFilterParserTest {

  @Test
  void readsTypesAndPlayer() {
    TransactionFilter filter =
        TransactionFilterParser.parse(Map.of("type", "VENDA,COMPRA", "player_id", "7"));

    assertThat(filter.types()).containsExactlyInAnyOrder(TransactionType.COMPRA, TransactionType.VENDA);
    assertThat(filter.playerId()).isEqualTo(7);
  }

  @Test
  void filtersNothingByDefault() {
    TransactionFilter filter = TransactionFilterParser.parse(Map.of("other", "x"));

    assertThat(filter.types()).isEmpty();
    assertThat(filter.playerId()).isNull();
  }

  @ParameterizedTest(name = "{0}={1}")
  @CsvSource(
      delimiter = '|',
      value = {
        "type|''",
        "type|venda",
        "type|COMPRA,venda",
        "type|COMPRA,COMPRA",
        "type|COMPRA, VENDA",
        "type|COMPRA,",
        "type|TROCA",
        "player_id|''",
        "player_id|0",
        "player_id|01",
        "player_id|abc"
      })
  void rejectsInvalidFilters(String name, String value) {
    assertThatThrownBy(() -> TransactionFilterParser.parse(Map.of(name, value)))
        .isInstanceOf(InvalidRequestException.class);
  }
}
