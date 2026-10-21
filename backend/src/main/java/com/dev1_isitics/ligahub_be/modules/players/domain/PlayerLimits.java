package com.dev1_isitics.ligahub_be.modules.players.domain;

/** Inclusive limits of the player fields (section "Limites de cada campo" of the statement). */
public final class PlayerLimits {

  public static final int NAME_MIN_LENGTH = 3;
  public static final int NAME_MAX_LENGTH = 100;
  public static final int AGILITY_MIN = 1;
  public static final int AGILITY_MAX = 100;
  public static final int AGE_MIN = 16;
  public static final int AGE_MAX = 45;
  public static final int ATTRIBUTE_MIN = 1;
  public static final int ATTRIBUTE_MAX = 99;
  public static final int ATTRIBUTES_MAX_SUM = 480;
  public static final int VETERAN_AGILITY_MIN_AGE = 35;
  public static final int VETERAN_AGILITY_MAX = 85;
  public static final int FIXED_POTENTIAL_MIN_AGE = 33;

  private PlayerLimits() {}
}
