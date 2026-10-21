package com.dev1_isitics.ligahub_be.modules.clubs.api;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ClubCreateRequestValidationTest {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  private boolean isValid(String name, String acronym) {
    return validator.validate(new ClubCreateRequest(name, acronym)).isEmpty();
  }

  @ParameterizedTest(name = "{0} / {1}")
  @CsvSource({
    "Águias FC, AGU",
    "Águias FC, AFC",
    "Conceição EC, CEC",
    "São-Paulo F.C., SPC",
    "Tubarões Esporte Clube Da Praia, TEC"
  })
  void acceptsValidNamesAndAcronyms(String name, String acronym) {
    assertThat(isValid(name, acronym)).isTrue();
  }

  @ParameterizedTest(name = "{0} / {1}")
  @CsvSource({
    "Águias, AGU",
    "Clube Um Dois Três Quatro Cinco, CUD",
    "Clube 1 FC, CFC",
    "Águias_FC, AGU",
    "Ab, ABX",
    "Águias FC, agu",
    "Águias FC, AG",
    "Águias FC, AGUA",
    "Águias FC, ÁGU",
    "Águias FC, GUA",
    "Águias FC, AUG",
    "Aaa Aaa, AAA",
    "Águias FC, AXY"
  })
  void rejectsInvalidNamesAndAcronyms(String name, String acronym) {
    assertThat(isValid(name, acronym)).isFalse();
  }
}
