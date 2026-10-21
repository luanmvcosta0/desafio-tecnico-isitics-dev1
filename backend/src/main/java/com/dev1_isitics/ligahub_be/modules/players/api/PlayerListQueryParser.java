package com.dev1_isitics.ligahub_be.modules.players.api;

import static com.dev1_isitics.ligahub_be.modules.players.domain.PlayerLimits.AGE_MAX;
import static com.dev1_isitics.ligahub_be.modules.players.domain.PlayerLimits.AGE_MIN;

import com.dev1_isitics.ligahub_be.modules.players.application.PlayerListQuery;
import com.dev1_isitics.ligahub_be.modules.players.application.PlayerSort;
import com.dev1_isitics.ligahub_be.modules.players.domain.Condition;
import com.dev1_isitics.ligahub_be.modules.players.domain.Position;
import com.dev1_isitics.ligahub_be.shared.exception.InvalidRequestException;
import com.dev1_isitics.ligahub_be.shared.text.TextNormalizer;
import com.dev1_isitics.ligahub_be.shared.web.QueryParams;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Reads and validates the query params of {@code GET /players}; unknown ones are ignored. */
public final class PlayerListQueryParser {

  private static final int SEARCH_MIN_LENGTH = 2;
  private static final int SEARCH_MAX_LENGTH = 100;
  private static final BigDecimal VALUE_MIN = BigDecimal.ZERO;
  private static final BigDecimal VALUE_MAX = new BigDecimal("550");
  private static final int MAX_SORTS = 2;
  private static final String SORT_SEPARATOR = ",";
  private static final int DEFAULT_PAGE = 1;
  private static final int DEFAULT_LIMIT = 10;
  private static final int LIMIT_MIN = 1;
  private static final int LIMIT_MAX = 50;

  private static final String SEARCH_MESSAGE =
      "O parâmetro search deve ter entre %d e %d caracteres, sem caracteres de controle."
          .formatted(SEARCH_MIN_LENGTH, SEARCH_MAX_LENGTH);
  private static final String SORT_COUNT_MESSAGE =
      "O parâmetro sort aceita no máximo " + MAX_SORTS + " critérios.";
  private static final String SORT_REPEATED_MESSAGE =
      "O parâmetro sort não pode repetir o mesmo campo (%s).";
  private static final String FREE_WITH_CLUB_MESSAGE =
      "Os filtros free=true e club_id são contraditórios.";
  private static final String RANGE_MESSAGE = "O parâmetro %s não pode ser maior que %s.";

  private PlayerListQueryParser() {}

  public static PlayerListQuery parse(Map<String, String> values) {
    QueryParams params = new QueryParams(values);
    String search = params.text("search").map(PlayerListQueryParser::parseSearch).orElse(null);
    Position position = params.enumValue("position", Position.class).orElse(null);
    Condition condition = params.enumValue("condition", Condition.class).orElse(null);
    Boolean free = params.bool("free").orElse(null);
    Integer clubId = params.positiveInteger("club_id").orElse(null);
    int minAge = params.integerBetween("min_age", AGE_MIN, AGE_MAX).orElse(AGE_MIN);
    int maxAge = params.integerBetween("max_age", AGE_MIN, AGE_MAX).orElse(AGE_MAX);
    BigDecimal minValue =
        params.decimalBetween("min_value", VALUE_MIN, VALUE_MAX).orElse(VALUE_MIN);
    BigDecimal maxValue =
        params.decimalBetween("max_value", VALUE_MIN, VALUE_MAX).orElse(VALUE_MAX);
    List<PlayerSort> sorts =
        params.text("sort").map(PlayerListQueryParser::parseSorts).orElse(List.of(PlayerSort.NAME));
    int page = params.positiveInteger("page").orElse(DEFAULT_PAGE);
    int limit = params.integerBetween("limit", LIMIT_MIN, LIMIT_MAX).orElse(DEFAULT_LIMIT);

    if (Boolean.TRUE.equals(free) && clubId != null) {
      throw new InvalidRequestException(FREE_WITH_CLUB_MESSAGE);
    }
    if (minAge > maxAge) {
      throw new InvalidRequestException(RANGE_MESSAGE.formatted("min_age", "max_age"));
    }
    if (minValue.compareTo(maxValue) > 0) {
      throw new InvalidRequestException(RANGE_MESSAGE.formatted("min_value", "max_value"));
    }
    return new PlayerListQuery(
        search, position, condition, free, clubId, minAge, maxAge, minValue, maxValue, sorts, page,
        limit);
  }

  private static String parseSearch(String value) {
    if (TextNormalizer.containsControlCharacter(value)) {
      throw new InvalidRequestException(SEARCH_MESSAGE);
    }
    String normalized = TextNormalizer.normalize(value);
    long length = normalized == null ? 0 : normalized.codePoints().count();
    if (length < SEARCH_MIN_LENGTH || length > SEARCH_MAX_LENGTH) {
      throw new InvalidRequestException(SEARCH_MESSAGE);
    }
    return normalized;
  }

  private static List<PlayerSort> parseSorts(String value) {
    String[] tokens = value.split(SORT_SEPARATOR, -1);
    if (tokens.length > MAX_SORTS) {
      throw new InvalidRequestException(SORT_COUNT_MESSAGE);
    }
    List<PlayerSort> sorts = new ArrayList<>();
    Set<String> fields = new HashSet<>();
    for (String token : tokens) {
      PlayerSort sort =
          PlayerSort.fromParameter(token).orElseThrow(() -> QueryParams.invalid("sort", value));
      if (!fields.add(sort.field())) {
        throw new InvalidRequestException(SORT_REPEATED_MESSAGE.formatted(sort.field()));
      }
      sorts.add(sort);
    }
    return sorts;
  }
}
