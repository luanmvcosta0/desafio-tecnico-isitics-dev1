package com.dev1_isitics.ligahub_be.shared.text;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class AlphabeticalComparatorTest {

  @Test
  void ordersIgnoringCaseAndAccents() {
    List<String> names = List.of("Gama SC", "épsilon EC", "Delta FC", "águias FC", "Mangue FC");

    assertThat(names.stream().sorted(AlphabeticalComparator.INSTANCE).toList())
        .containsExactly("águias FC", "Delta FC", "épsilon EC", "Gama SC", "Mangue FC");
  }

  @Test
  void putsSpaceBeforeHyphenAndHyphenBeforeLetters() {
    List<String> names = List.of("Ana-Maria", "Anabela", "Ana Lima");

    assertThat(names.stream().sorted(AlphabeticalComparator.INSTANCE).toList())
        .containsExactly("Ana Lima", "Ana-Maria", "Anabela");
  }

  @ParameterizedTest
  @CsvSource({"Águias FC, Aguias FC", "Conceição, CONCEICAO", "João, joao"})
  void tiesNamesEqualUnderTheRule(String first, String second) {
    assertThat(AlphabeticalComparator.INSTANCE.compare(first, second)).isZero();
  }
}
