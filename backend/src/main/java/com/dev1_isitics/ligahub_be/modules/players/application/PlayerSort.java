package com.dev1_isitics.ligahub_be.modules.players.application;

import com.dev1_isitics.ligahub_be.modules.players.domain.Player;
import com.dev1_isitics.ligahub_be.shared.text.AlphabeticalComparator;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

/**
 * Sort criteria of {@code GET /players} (RN07). Value sorts use the market value; position follows
 * the squad order GOL, ZAG, LAT, MEI, ATA. Each criterion belongs to a field that cannot repeat.
 */
public enum PlayerSort {
  NAME("name", "name", Comparator.comparing(Player::getName, AlphabeticalComparator.INSTANCE)),
  VALUE_ASC("value_asc", "value", Comparator.comparing(Player::marketValue)),
  VALUE_DESC("value_desc", "value", Comparator.comparing(Player::marketValue).reversed()),
  AGE_ASC("age_asc", "age", Comparator.comparingInt(Player::getAge)),
  AGE_DESC("age_desc", "age", Comparator.comparingInt(Player::getAge).reversed()),
  POSITION("position", "position", Comparator.comparing(Player::getPosition));

  /** Tie-break of every listing: alphabetical name, then ascending id. */
  public static final Comparator<Player> TIE_BREAK =
      NAME.comparator.thenComparing(Player::getId);

  /** Squad order (RN11): position GOL, ZAG, LAT, MEI, ATA, then name, then id. */
  public static final Comparator<Player> SQUAD_ORDER = POSITION.comparator.thenComparing(TIE_BREAK);

  private final String parameter;
  private final String field;
  private final Comparator<Player> comparator;

  PlayerSort(String parameter, String field, Comparator<Player> comparator) {
    this.parameter = parameter;
    this.field = field;
    this.comparator = comparator;
  }

  public static Optional<PlayerSort> fromParameter(String value) {
    return Arrays.stream(values()).filter(sort -> sort.parameter.equals(value)).findFirst();
  }

  public String field() {
    return field;
  }

  public Comparator<Player> comparator() {
    return comparator;
  }
}
