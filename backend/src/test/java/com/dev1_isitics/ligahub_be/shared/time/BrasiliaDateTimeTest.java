package com.dev1_isitics.ligahub_be.shared.time;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class BrasiliaDateTimeTest {

  @Test
  void parsesTheRequestFormatInBrasiliaTime() {
    assertThat(BrasiliaDateTime.parseRequest("2027-03-02T10:15-03:00"))
        .contains(Instant.parse("2027-03-02T13:15:00Z"));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "2027-03-02T10:15",
        "2027-03-02T10:15Z",
        "2027-03-02T10:15-02:00",
        "2027-03-02T10:15:00-03:00",
        "2027-3-02T10:15-03:00",
        "2027-03-02 10:15-03:00",
        "2027-02-30T10:15-03:00",
        "2027-02-29T10:15-03:00",
        "2027-03-02T24:00-03:00",
        "2027-03-02T10:60-03:00",
        ""
      })
  void refusesOtherFormatsAndNonexistentDates(String value) {
    assertThat(BrasiliaDateTime.parseRequest(value)).isEmpty();
  }

  @Test
  void acceptsLeapDay() {
    assertThat(BrasiliaDateTime.parseRequest("2028-02-29T00:00-03:00")).isPresent();
  }

  @Test
  void formatsResponsesWithSecondsAndOffset() {
    assertThat(BrasiliaDateTime.format(Instant.parse("2026-11-02T13:15:42.987Z")))
        .isEqualTo("2026-11-02T10:15:42-03:00");
  }
}
