package com.dev1_isitics.ligahub_be.modules.players.api;

import com.dev1_isitics.ligahub_be.modules.players.domain.Condition;
import com.dev1_isitics.ligahub_be.modules.players.domain.Player;
import com.dev1_isitics.ligahub_be.modules.players.domain.Position;
import com.dev1_isitics.ligahub_be.modules.players.domain.TechnicalAttributes;
import java.math.BigDecimal;

/** {@code PlayerResponse} of the contract, with overall and market value derived on each read. */
public record PlayerResponse(
    Integer id,
    String name,
    Position position,
    int agility,
    int age,
    Condition condition,
    BigDecimal baseValue,
    BigDecimal marketValue,
    int pace,
    int shooting,
    int passing,
    int dribbling,
    int defending,
    int physical,
    int potential,
    int overall,
    Integer clubId) {

  public static PlayerResponse from(Player player) {
    TechnicalAttributes attributes = player.getAttributes();
    return new PlayerResponse(
        player.getId(),
        player.getName(),
        player.getPosition(),
        player.getAgility(),
        player.getAge(),
        player.getCondition(),
        player.getBaseValue(),
        player.marketValue(),
        attributes.getPace(),
        attributes.getShooting(),
        attributes.getPassing(),
        attributes.getDribbling(),
        attributes.getDefending(),
        attributes.getPhysical(),
        player.getPotential(),
        player.overall(),
        player.getClubId());
  }
}
