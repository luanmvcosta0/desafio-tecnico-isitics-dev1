package com.dev1_isitics.ligahub_be.modules.clubs.application;

import com.dev1_isitics.ligahub_be.modules.clubs.domain.ClubValuation;
import com.dev1_isitics.ligahub_be.shared.text.AlphabeticalComparator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

/** Sort of {@code GET /clubs} (RN11): ties are broken by alphabetical name, then ascending id. */
public enum ClubSort {
  NAME(
      "name",
      Comparator.comparing(
          valuation -> valuation.club().getName(), AlphabeticalComparator.INSTANCE)),
  NET_WORTH_ASC("net_worth_asc", Comparator.comparing(ClubValuation::netWorth)),
  NET_WORTH_DESC("net_worth_desc", Comparator.comparing(ClubValuation::netWorth).reversed());

  private static final Comparator<ClubValuation> TIE_BREAK =
      NAME.comparator.thenComparing(valuation -> valuation.club().getId());

  private final String parameter;
  private final Comparator<ClubValuation> comparator;

  ClubSort(String parameter, Comparator<ClubValuation> comparator) {
    this.parameter = parameter;
    this.comparator = comparator;
  }

  public static Optional<ClubSort> fromParameter(String value) {
    return Arrays.stream(values()).filter(sort -> sort.parameter.equals(value)).findFirst();
  }

  public Comparator<ClubValuation> withTieBreak() {
    return comparator.thenComparing(TIE_BREAK);
  }
}
