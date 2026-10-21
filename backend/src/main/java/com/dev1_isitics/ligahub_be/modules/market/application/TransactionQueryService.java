package com.dev1_isitics.ligahub_be.modules.market.application;

import com.dev1_isitics.ligahub_be.modules.clubs.domain.ClubNotFoundException;
import com.dev1_isitics.ligahub_be.modules.clubs.infrastructure.ClubRepository;
import com.dev1_isitics.ligahub_be.modules.market.domain.Transaction;
import com.dev1_isitics.ligahub_be.modules.market.domain.TransactionSummary;
import com.dev1_isitics.ligahub_be.modules.market.infrastructure.TransactionRepository;
import com.dev1_isitics.ligahub_be.modules.market.infrastructure.TransactionSpecifications;
import com.dev1_isitics.ligahub_be.modules.players.domain.PlayerNotFoundException;
import com.dev1_isitics.ligahub_be.modules.players.infrastructure.PlayerRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Transaction history of a club (RN17), shared by the JSON listing and the CSV export (RN18). A
 * deleted player counts as nonexistent in the {@code player_id} filter; old transactions stay.
 */
@Service
@RequiredArgsConstructor
public class TransactionQueryService {

  private final ClubRepository clubRepository;
  private final PlayerRepository playerRepository;
  private final TransactionRepository transactionRepository;

  @Transactional(readOnly = true)
  public TransactionHistory history(Integer clubId, TransactionFilter filter) {
    if (!clubRepository.existsById(clubId)) {
      throw new ClubNotFoundException(clubId);
    }
    if (filter.playerId() != null
        && playerRepository.findByIdAndDeletedAtIsNull(filter.playerId()).isEmpty()) {
      throw new PlayerNotFoundException(filter.playerId());
    }
    List<Transaction> transactions =
        transactionRepository.findAll(
            TransactionSpecifications.ofClub(clubId, filter),
            TransactionSpecifications.HISTORY_ORDER);
    return new TransactionHistory(transactions, TransactionSummary.of(transactions));
  }
}
