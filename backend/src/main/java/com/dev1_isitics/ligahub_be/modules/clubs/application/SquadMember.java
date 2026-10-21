package com.dev1_isitics.ligahub_be.modules.clubs.application;

import com.dev1_isitics.ligahub_be.modules.market.domain.Contract;
import com.dev1_isitics.ligahub_be.modules.players.domain.Player;

/**
 * Player of a squad with the active contract and, computed now, the remaining loyalty days and the
 * penalty rate if the player were sold at this moment (RN11, RN15).
 */
public record SquadMember(Player player, Contract contract, long remainingDays, int penaltyRate) {}
