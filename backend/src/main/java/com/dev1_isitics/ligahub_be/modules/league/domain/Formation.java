package com.dev1_isitics.ligahub_be.modules.league.domain;

import com.dev1_isitics.ligahub_be.modules.players.domain.Position;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Formations of the 4 outfield players, counted by sector from back to front (RN19): defense (ZAG
 * or LAT), midfield (MEI) and attack (ATA), always plus exactly one goalkeeper.
 */
public enum Formation {
  THREE_ONE("3-1", 3, 0, 1),
  TWO_TWO("2-2", 2, 0, 2),
  TWO_ONE_ONE("2-1-1", 2, 1, 1),
  ONE_TWO_ONE("1-2-1", 1, 2, 1);

  public static final int GOALKEEPERS = 1;
  public static final int STARTERS = 5;

  private final String code;
  private final int defenders;
  private final int midfielders;
  private final int attackers;

  Formation(String code, int defenders, int midfielders, int attackers) {
    this.code = code;
    this.defenders = defenders;
    this.midfielders = midfielders;
    this.attackers = attackers;
  }

  /** Exact code of the statement ("2-1-1"); anything else is not a formation (V5). */
  @JsonCreator
  public static Formation fromCode(String code) {
    return Arrays.stream(values())
        .filter(formation -> formation.code.equals(code))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Formação inválida: " + code));
  }

  @JsonValue
  public String code() {
    return code;
  }

  /** Whether the positions of the five starters close this formation. */
  public boolean isClosedBy(Collection<Position> positions) {
    Map<Position, Long> counts =
        positions.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
    return positions.size() == STARTERS
        && count(counts, Position.GOL) == GOALKEEPERS
        && count(counts, Position.ZAG) + count(counts, Position.LAT) == defenders
        && count(counts, Position.MEI) == midfielders
        && count(counts, Position.ATA) == attackers;
  }

  private static long count(Map<Position, Long> counts, Position position) {
    return counts.getOrDefault(position, 0L);
  }
}
