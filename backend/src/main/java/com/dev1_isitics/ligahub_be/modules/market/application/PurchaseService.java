package com.dev1_isitics.ligahub_be.modules.market.application;

import com.dev1_isitics.ligahub_be.modules.clubs.domain.Club;
import com.dev1_isitics.ligahub_be.modules.clubs.domain.ClubNotFoundException;
import com.dev1_isitics.ligahub_be.modules.clubs.infrastructure.ClubRepository;
import com.dev1_isitics.ligahub_be.modules.market.domain.Contract;
import com.dev1_isitics.ligahub_be.modules.market.domain.MarketLimits;
import com.dev1_isitics.ligahub_be.modules.market.domain.PurchaseFeeCalculator;
import com.dev1_isitics.ligahub_be.modules.market.domain.Transaction;
import com.dev1_isitics.ligahub_be.modules.market.domain.TransactionType;
import com.dev1_isitics.ligahub_be.modules.market.infrastructure.ContractRepository;
import com.dev1_isitics.ligahub_be.modules.market.infrastructure.TransactionRepository;
import com.dev1_isitics.ligahub_be.modules.players.domain.Player;
import com.dev1_isitics.ligahub_be.modules.players.domain.PlayerNotFoundException;
import com.dev1_isitics.ligahub_be.modules.players.infrastructure.PlayerRepository;
import com.dev1_isitics.ligahub_be.shared.exception.BusinessRuleException;
import com.dev1_isitics.ligahub_be.shared.exception.InvalidRequestException;
import com.dev1_isitics.ligahub_be.shared.money.Money;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Purchase of a free player (RN12–RN14), checked in the order 400 → 404 → 409. Cash, squad,
 * contract and transaction are written in a single database transaction.
 */
@Service
@RequiredArgsConstructor
public class PurchaseService {

  private static final Duration MAX_LOYALTY = Duration.ofDays(MarketLimits.MAX_LOYALTY_DAYS);
  private static final String LOYALTY_RANGE_MESSAGE =
      "O fim da fidelidade (loyalty_until) deve ser depois do momento da compra e até "
          + MarketLimits.MAX_LOYALTY_DAYS
          + " dias depois dele.";
  private static final String PLAYER_NOT_FREE_MESSAGE =
      "O jogador %s já pertence a um clube; só jogadores livres podem ser comprados.";
  private static final String REBUY_MESSAGE =
      "O clube %s já vendeu o jogador %s e não pode comprá-lo de novo.";
  private static final String SQUAD_FULL_MESSAGE =
      "O elenco do clube %s já tem o máximo de " + MarketLimits.MAX_SQUAD_SIZE + " jogadores.";
  private static final String CASH_LIMIT_MESSAGE =
      "O custo da compra (%s) passa do limite de 50%% do caixa (%s).";

  private final PlayerRepository playerRepository;
  private final ClubRepository clubRepository;
  private final ContractRepository contractRepository;
  private final TransactionRepository transactionRepository;
  private final Clock clock;

  @Transactional
  public Purchase buy(Integer playerId, Integer clubId, Instant loyaltyUntil) {
    Instant now = clock.instant();
    requireLoyaltyWithinRange(loyaltyUntil, now);

    Player player =
        playerRepository
            .findActiveByIdForUpdate(playerId)
            .orElseThrow(() -> new PlayerNotFoundException(playerId));
    Club club =
        clubRepository
            .findByIdForUpdate(clubId)
            .orElseThrow(() -> new ClubNotFoundException(clubId));

    BigDecimal value = player.marketValue();
    BigDecimal fee = PurchaseFeeCalculator.fee(value);
    BigDecimal totalCost = value.add(fee);
    requirePurchaseAllowed(player, club, totalCost);

    club.changeCash(totalCost.negate());
    player.joinClub(club.getId());
    Contract contract =
        contractRepository.save(
            Contract.sign(player.getId(), club.getId(), value, loyaltyUntil, now));
    Transaction transaction =
        transactionRepository.save(
            Transaction.purchase(club.getId(), player.getId(), player.getName(), value, fee, now));
    return new Purchase(transaction, contract, club.getCash());
  }

  private static void requireLoyaltyWithinRange(Instant loyaltyUntil, Instant now) {
    if (!loyaltyUntil.isAfter(now) || loyaltyUntil.isAfter(now.plus(MAX_LOYALTY))) {
      throw new InvalidRequestException(LOYALTY_RANGE_MESSAGE);
    }
  }

  private void requirePurchaseAllowed(Player player, Club club, BigDecimal totalCost) {
    if (!player.isFree()) {
      throw new BusinessRuleException(PLAYER_NOT_FREE_MESSAGE.formatted(player.getName()));
    }
    if (transactionRepository.existsByClubIdAndPlayerIdAndType(
        club.getId(), player.getId(), TransactionType.VENDA)) {
      throw new BusinessRuleException(REBUY_MESSAGE.formatted(club.getName(), player.getName()));
    }
    if (playerRepository.countByClubIdAndDeletedAtIsNull(club.getId())
        >= MarketLimits.MAX_SQUAD_SIZE) {
      throw new BusinessRuleException(SQUAD_FULL_MESSAGE.formatted(club.getName()));
    }
    if (!PurchaseFeeCalculator.isAffordable(totalCost, club.getCash())) {
      throw new BusinessRuleException(
          CASH_LIMIT_MESSAGE.formatted(
              Money.display(totalCost),
              Money.display(PurchaseFeeCalculator.cashLimit(club.getCash()))));
    }
  }
}
