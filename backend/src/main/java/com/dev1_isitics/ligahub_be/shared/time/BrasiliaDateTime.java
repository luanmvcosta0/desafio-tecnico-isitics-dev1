package com.dev1_isitics.ligahub_be.shared.time;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Dates and times of the API, always in Brasília time (-03:00, no daylight saving time, RN01).
 * Requests use exactly {@code AAAA-MM-DDTHH:MM-03:00}; responses use {@code
 * AAAA-MM-DDTHH:MM:SS-03:00}. Nonexistent dates are refused, never adjusted.
 */
public final class BrasiliaDateTime {

  public static final ZoneOffset OFFSET = ZoneOffset.ofHours(-3);

  private static final Pattern REQUEST_PATTERN =
      Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}-03:00");
  private static final DateTimeFormatter REQUEST_FORMAT =
      DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mmXXX").withResolverStyle(ResolverStyle.STRICT);
  private static final DateTimeFormatter RESPONSE_FORMAT =
      DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssXXX");

  private BrasiliaDateTime() {}

  /** The instant of a request date-time, or empty when it is not in the RN01 format. */
  public static Optional<Instant> parseRequest(String value) {
    if (value == null || !REQUEST_PATTERN.matcher(value).matches()) {
      return Optional.empty();
    }
    try {
      return Optional.of(OffsetDateTime.parse(value, REQUEST_FORMAT).toInstant());
    } catch (DateTimeParseException exception) {
      return Optional.empty();
    }
  }

  public static String format(Instant instant) {
    return instant.truncatedTo(ChronoUnit.SECONDS).atOffset(OFFSET).format(RESPONSE_FORMAT);
  }
}
