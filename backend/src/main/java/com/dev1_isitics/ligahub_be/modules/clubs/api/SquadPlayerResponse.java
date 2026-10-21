package com.dev1_isitics.ligahub_be.modules.clubs.api;

import com.dev1_isitics.ligahub_be.modules.clubs.application.SquadMember;
import com.dev1_isitics.ligahub_be.modules.players.api.PlayerResponse;
import com.dev1_isitics.ligahub_be.shared.time.BrasiliaDateTime;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.math.BigDecimal;

/** Player of the squad: every field of {@code PlayerResponse} plus the {@code contract} (RN11). */
public record SquadPlayerResponse(@JsonUnwrapped PlayerResponse player, ContractResponse contract) {

  public record ContractResponse(
      BigDecimal purchaseValue, String loyaltyUntil, long remainingDays, int penaltyRate) {}

  public static SquadPlayerResponse from(SquadMember member) {
    ContractResponse contract =
        member.contract() == null
            ? null
            : new ContractResponse(
                member.contract().getPurchaseValue(),
                BrasiliaDateTime.format(member.contract().getLoyaltyUntil()),
                member.remainingDays(),
                member.penaltyRate());
    return new SquadPlayerResponse(PlayerResponse.from(member.player()), contract);
  }
}
