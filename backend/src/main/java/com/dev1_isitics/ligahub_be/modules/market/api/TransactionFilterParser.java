package com.dev1_isitics.ligahub_be.modules.market.api;

import com.dev1_isitics.ligahub_be.modules.market.application.TransactionFilter;
import com.dev1_isitics.ligahub_be.modules.market.domain.TransactionType;
import com.dev1_isitics.ligahub_be.shared.exception.InvalidRequestException;
import com.dev1_isitics.ligahub_be.shared.web.QueryParams;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Filters of the history and of the export: {@code type} with one or more of COMPRA and VENDA,
 * comma separated, without spaces or repetition, and {@code player_id}. Same rules for both.
 */
public final class TransactionFilterParser {

  private static final String TYPE = "type";
  private static final String PLAYER_ID = "player_id";
  private static final String TYPE_SEPARATOR = ",";
  private static final String REPEATED_TYPE_MESSAGE =
      "O parâmetro type não pode repetir o mesmo tipo (%s).";

  private TransactionFilterParser() {}

  public static TransactionFilter parse(Map<String, String> values) {
    QueryParams params = new QueryParams(values);
    Set<TransactionType> types =
        params.text(TYPE).map(TransactionFilterParser::parseTypes).orElse(Set.of());
    Integer playerId = params.positiveInteger(PLAYER_ID).orElse(null);
    return new TransactionFilter(types, playerId);
  }

  private static Set<TransactionType> parseTypes(String value) {
    Set<TransactionType> types = EnumSet.noneOf(TransactionType.class);
    for (String token : value.split(TYPE_SEPARATOR, -1)) {
      TransactionType type =
          Arrays.stream(TransactionType.values())
              .filter(candidate -> candidate.name().equals(token))
              .findFirst()
              .orElseThrow(() -> QueryParams.invalid(TYPE, value));
      if (!types.add(type)) {
        throw new InvalidRequestException(REPEATED_TYPE_MESSAGE.formatted(type));
      }
    }
    return types;
  }
}
