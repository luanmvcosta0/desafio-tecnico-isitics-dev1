package com.dev1_isitics.ligahub_be.modules.clubs.domain;

import com.dev1_isitics.ligahub_be.modules.players.domain.Player;
import com.dev1_isitics.ligahub_be.shared.money.Money;
import java.math.BigDecimal;
import java.util.List;

/**
 * Values of a club computed on every read and never stored (RN11): the squad value is the sum of
 * the market values of its players and the net worth is cash plus squad value.
 */
public record ClubValuation(Club club, List<Player> squad, BigDecimal squadValue, BigDecimal netWorth) {

  public static ClubValuation of(Club club, List<Player> squad) {
    BigDecimal squadValue =
        Money.round(squad.stream().map(Player::marketValue).reduce(BigDecimal.ZERO, BigDecimal::add));
    return new ClubValuation(club, squad, squadValue, club.getCash().add(squadValue));
  }
}
