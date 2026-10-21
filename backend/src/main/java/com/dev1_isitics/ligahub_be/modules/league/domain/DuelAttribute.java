package com.dev1_isitics.ligahub_be.modules.league.domain;

import com.dev1_isitics.ligahub_be.modules.players.domain.Position;
import com.dev1_isitics.ligahub_be.modules.players.domain.TechnicalAttributes;
import java.util.function.ToIntFunction;

/**
 * Attribute used in a duel (RN20): the attacker uses the one of its position (ATA shooting, MEI
 * passing, ZAG/LAT dribbling) and the defender always uses defending, goalkeeper included.
 */
public enum DuelAttribute {
  SHOOTING("shooting", TechnicalAttributes::getShooting),
  PASSING("passing", TechnicalAttributes::getPassing),
  DRIBBLING("dribbling", TechnicalAttributes::getDribbling),
  DEFENDING("defending", TechnicalAttributes::getDefending);

  private final String code;
  private final ToIntFunction<TechnicalAttributes> reader;

  DuelAttribute(String code, ToIntFunction<TechnicalAttributes> reader) {
    this.code = code;
    this.reader = reader;
  }

  public static DuelAttribute ofAttacker(Position position) {
    return switch (position) {
      case ATA -> SHOOTING;
      case MEI -> PASSING;
      case ZAG, LAT -> DRIBBLING;
      case GOL -> throw new IllegalArgumentException("O goleiro não ataca.");
    };
  }

  public String code() {
    return code;
  }

  public int read(TechnicalAttributes attributes) {
    return reader.applyAsInt(attributes);
  }
}
