package com.dev1_isitics.ligahub_be.shared.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class TextNormalizerTest {

  @ParameterizedTest(name = "\"{0}\" -> \"{1}\"")
  @CsvSource(
      delimiter = '|',
      value = {"'  Meia   Zeta '|Meia Zeta", "Ana Lima|Ana Lima", "' Zé  Foguete'|Zé Foguete"})
  void trimsAndCollapsesSpaces(String text, String expected) {
    assertThat(TextNormalizer.normalize(text)).isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(strings = {"", " ", "    "})
  void treatsBlankTextAsAbsent(String text) {
    assertThat(TextNormalizer.normalize(text)).isNull();
  }

  @ParameterizedTest
  @ValueSource(strings = {"Meia\tLua", "Meia\nLua", "Meia\u0000Lua", "Meia\u007FLua"})
  void detectsControlCharacters(String text) {
    assertThat(TextNormalizer.containsControlCharacter(text)).isTrue();
  }

  @Test
  void acceptsAccentsAndPunctuation() {
    assertThat(TextNormalizer.containsControlCharacter("João D'Ávila-Neto")).isFalse();
  }
}
