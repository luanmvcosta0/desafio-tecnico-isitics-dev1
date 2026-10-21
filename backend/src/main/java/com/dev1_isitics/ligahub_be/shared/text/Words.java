package com.dev1_isitics.ligahub_be.shared.text;

import java.util.Arrays;

/** Words of a normalized name: the sequences between spaces that hold at least one letter. */
public final class Words {

  private static final String SPACE = " ";

  private Words() {}

  public static long count(String text) {
    return Arrays.stream(text.split(SPACE))
        .filter(token -> token.codePoints().anyMatch(Character::isLetter))
        .count();
  }
}
