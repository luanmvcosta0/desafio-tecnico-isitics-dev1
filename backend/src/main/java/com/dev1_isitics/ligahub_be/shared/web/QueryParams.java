package com.dev1_isitics.ligahub_be.shared.web;

import com.dev1_isitics.ligahub_be.shared.exception.InvalidRequestException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.web.context.request.NativeWebRequest;

/**
 * Strict readers of query params shared by the listing endpoints. An absent parameter means "use
 * the default"; a present one must be exactly in the accepted format (empty value, other letter
 * case, sign, leading zeros or exponent are 400).
 */
public final class QueryParams {

  private static final Pattern POSITIVE_INTEGER = Pattern.compile("[1-9][0-9]*");
  private static final Pattern DECIMAL = Pattern.compile("(0|[1-9][0-9]*)(\\.[0-9]{1,2})?");
  private static final String EMPTY_MESSAGE = "O parâmetro %s não pode ser vazio.";
  private static final String INVALID_MESSAGE = "O parâmetro %s tem valor inválido: %s.";
  private static final String OUT_OF_RANGE_MESSAGE = "O parâmetro %s deve estar entre %s e %s.";
  private static final String BOOLEAN_TRUE = "true";
  private static final String BOOLEAN_FALSE = "false";

  private final Map<String, String> values;

  public QueryParams(Map<String, String> values) {
    this.values = values;
  }

  /** The first value of each query param of the request; repeated params keep only the first. */
  public static Map<String, String> firstValues(NativeWebRequest request) {
    return request.getParameterMap().entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue()[0]));
  }

  public static QueryParams of(NativeWebRequest request) {
    return new QueryParams(firstValues(request));
  }

  public Optional<String> text(String name) {
    String value = values.get(name);
    if (value != null && value.isEmpty()) {
      throw new InvalidRequestException(EMPTY_MESSAGE.formatted(name));
    }
    return Optional.ofNullable(value);
  }

  public Optional<Integer> positiveInteger(String name) {
    return text(name).map(value -> parsePositiveInteger(name, value));
  }

  public Optional<Integer> integerBetween(String name, int min, int max) {
    return positiveInteger(name).map(value -> requireBetween(name, value, min, max));
  }

  public Optional<BigDecimal> decimalBetween(String name, BigDecimal min, BigDecimal max) {
    return text(name)
        .map(
            value -> {
              if (!DECIMAL.matcher(value).matches()) {
                throw invalid(name, value);
              }
              BigDecimal decimal = new BigDecimal(value);
              if (decimal.compareTo(min) < 0 || decimal.compareTo(max) > 0) {
                throw new InvalidRequestException(
                    OUT_OF_RANGE_MESSAGE.formatted(name, min.toPlainString(), max.toPlainString()));
              }
              return decimal;
            });
  }

  public Optional<Boolean> bool(String name) {
    return text(name)
        .map(
            value -> {
              if (!value.equals(BOOLEAN_TRUE) && !value.equals(BOOLEAN_FALSE)) {
                throw invalid(name, value);
              }
              return value.equals(BOOLEAN_TRUE);
            });
  }

  /** Enum value written exactly as its constant (V5: case-sensitive). */
  public <E extends Enum<E>> Optional<E> enumValue(String name, Class<E> type) {
    return text(name)
        .map(
            value ->
                Arrays.stream(type.getEnumConstants())
                    .filter(constant -> constant.name().equals(value))
                    .findFirst()
                    .orElseThrow(() -> invalid(name, value)));
  }

  public static InvalidRequestException invalid(String name, String value) {
    return new InvalidRequestException(INVALID_MESSAGE.formatted(name, value));
  }

  private static Integer parsePositiveInteger(String name, String value) {
    if (!POSITIVE_INTEGER.matcher(value).matches()) {
      throw invalid(name, value);
    }
    try {
      return Integer.valueOf(value);
    } catch (NumberFormatException exception) {
      throw invalid(name, value);
    }
  }

  private static Integer requireBetween(String name, int value, int min, int max) {
    if (value < min || value > max) {
      throw new InvalidRequestException(OUT_OF_RANGE_MESSAGE.formatted(name, min, max));
    }
    return value;
  }
}
