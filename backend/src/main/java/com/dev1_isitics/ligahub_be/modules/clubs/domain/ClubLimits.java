package com.dev1_isitics.ligahub_be.modules.clubs.domain;

import java.math.BigDecimal;

/** Limits of the clubs (RN10) and of their fields. */
public final class ClubLimits {

  public static final BigDecimal INITIAL_CASH = new BigDecimal("300.00");
  public static final int MAX_CLUBS = 20;
  public static final int NAME_MIN_LENGTH = 3;
  public static final int NAME_MAX_LENGTH = 60;
  public static final int NAME_MIN_WORDS = 2;
  public static final int NAME_MAX_WORDS = 5;
  public static final String ACRONYM_PATTERN = "[A-Z]{3}";

  private ClubLimits() {}
}
