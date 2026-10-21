package com.dev1_isitics.ligahub_be.modules.market.application;

import com.dev1_isitics.ligahub_be.modules.clubs.domain.Club;
import com.dev1_isitics.ligahub_be.modules.clubs.infrastructure.ClubRepository;
import com.dev1_isitics.ligahub_be.modules.market.domain.Contract;
import com.dev1_isitics.ligahub_be.modules.market.domain.ContractStatus;
import com.dev1_isitics.ligahub_be.modules.market.domain.LoyaltyPenaltyCalculator;
import com.dev1_isitics.ligahub_be.modules.market.domain.SaleCommissionCalculator;
import com.dev1_isitics.ligahub_be.modules.market.domain.Transaction;
import com.dev1_isitics.ligahub_be.modules.market.infrastructure.ContractRepository;
import com.dev1_isitics.ligahub_be.modules.market.infrastructure.TransactionRepository;
import com.dev1_isitics.ligahub_be.modules.players.domain.Player;
import com.dev1_isitics.ligahub_be.modules.players.infrastructure.PlayerRepository;
import com.dev1_isitics.ligahub_be.shared.exception.BusinessRuleException;
import com.dev1_isitics.ligahub_be.shared.exception.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Sale of a player by its current club (RN15, RN16): penalty for breaking the loyalty, progressive
 * commission on the profit and credit to the cash. Player, contract, cash and transaction are
 * written in a single database transaction.
 */
@Service
@RequiredArgsConstructor
public class SaleService {

  private static final String PLAYER_NOT_FOUND_MESSAGE = "Jogador %d não encontrado.";
  private static final String FREE_PLAYER_MESSAGE =
      "O jogador %s está livre; só jogadores de um clube podem ser vendidos.";
  private static final String MISSING_CONTRACT_MESSAGE =
      "Jogador %d está num clube sem contrato ativo.";

  private final PlayerRepository playerRepository;
  private final ClubRepository clubRepository;
  private final ContractRepository contractRepository;
  private final TransactionRepository transactionRepository;
  private final Clock clock;

  @Transactional
  public Sale sell(Integer playerId) {
    Player player =
        playerRepository
            .findActiveByIdForUpdate(playerId)
            .orElseThrow(
                () -> new ResourceNotFoundException(PLAYER_NOT_FOUND_MESSAGE.formatted(playerId)));
    if (player.isFree()) {
      throw new BusinessRuleException(FREE_PLAYER_MESSAGE.formatted(player.getName()));
    }
    Club club = clubRepository.findByIdForUpdate(player.getClubId()).orElseThrow();
    Contract contract =
        contractRepository
            .findByPlayerIdAndStatus(playerId, ContractStatus.ATIVO)
            .orElseThrow(
                () -> new IllegalStateException(MISSING_CONTRACT_MESSAGE.formatted(playerId)));

    Instant now = clock.instant();
    BigDecimal value = player.marketValue();
    long remainingDays = LoyaltyPenaltyCalculator.remainingDays(contract.getLoyaltyUntil(), now);
    int penaltyRate = LoyaltyPenaltyCalculator.penaltyRate(remainingDays);
    BigDecimal penalty = LoyaltyPenaltyCalculator.penalty(value, penaltyRate);
    BigDecimal profit = SaleCommissionCalculator.profit(value, contract.getPurchaseValue());
    BigDecimal commission = SaleCommissionCalculator.commission(profit);

    Transaction transaction =
        transactionRepository.save(
            Transaction.sale(
                club.getId(),
                player.getId(),
                player.getName(),
                value,
                commission,
                penalty,
                profit,
                now));
    club.changeCash(transaction.getCashChange());
    player.leaveClub();
    contract.end(penaltyRate > 0, now);
    return new Sale(transaction, contract, remainingDays, penaltyRate, club.getCash());
  }
}
