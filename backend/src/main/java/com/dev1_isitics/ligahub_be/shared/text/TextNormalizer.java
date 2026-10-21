package com.dev1_isitics.ligahub_be.shared.text;

import java.util.regex.Pattern;

/**
 * Text normalization of names and searches (V4): leading and trailing spaces removed and repeated
 * spaces collapsed into one. Only U+0020 counts as a space; any other control character is invalid.
 */
public final class TextNormalizer {

  private static final Pattern REPEATED_SPACES = Pattern.compile(" {2,}");
  private static final char SPACE = ' ';

  private TextNormalizer() {}

  public static boolean containsControlCharacter(String text) {
    return text.codePoints().anyMatch(Character::isISOControl);
  }

  /** Returns the normalized text, or {@code null} when nothing but spaces remains. */
  public static String normalize(String text) {
    String collapsed = REPEATED_SPACES.matcher(strip(text)).replaceAll(String.valueOf(SPACE));
    return collapsed.isEmpty() ? null : collapsed;
  }

  private static String strip(String text) {
    int start = 0;
    int end = text.length();
    while (start < end && text.charAt(start) == SPACE) {
      start++;
    }
    while (end > start && text.charAt(end - 1) == SPACE) {
      end--;
    }
    return text.substring(start, end);
  }
}
