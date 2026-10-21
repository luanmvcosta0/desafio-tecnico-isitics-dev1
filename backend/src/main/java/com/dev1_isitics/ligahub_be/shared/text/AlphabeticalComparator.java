package com.dev1_isitics.ligahub_be.shared.text;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Alphabetical order of the statement: lower case, accents removed (NFD without the Unicode Mn
 * marks, cedilla included) and comparison code point by code point. Names equal under this rule tie
 * (the caller breaks ties by id). Done in Java because the database collation follows other rules.
 */
public final class AlphabeticalComparator implements Comparator<String> {

  public static final AlphabeticalComparator INSTANCE = new AlphabeticalComparator();

  private static final Pattern NONSPACING_MARKS = Pattern.compile("\\p{Mn}+");

  private AlphabeticalComparator() {}

  /** Lower case text without accents, also used by the accent-insensitive search (RN07). */
  public static String toComparisonKey(String text) {
    String decomposed = Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
    return NONSPACING_MARKS.matcher(decomposed).replaceAll("");
  }

  @Override
  public int compare(String first, String second) {
    return compareCodePoints(toComparisonKey(first), toComparisonKey(second));
  }

  private static int compareCodePoints(String first, String second) {
    int[] firstCodePoints = first.codePoints().toArray();
    int[] secondCodePoints = second.codePoints().toArray();
    return Arrays.compare(firstCodePoints, secondCodePoints);
  }
}
