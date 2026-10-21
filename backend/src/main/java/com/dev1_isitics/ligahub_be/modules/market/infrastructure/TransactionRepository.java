package com.dev1_isitics.ligahub_be.modules.market.infrastructure;

import com.dev1_isitics.ligahub_be.modules.market.domain.Transaction;
import com.dev1_isitics.ligahub_be.modules.market.domain.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

  boolean existsByClubIdAndPlayerIdAndType(Integer clubId, Integer playerId, TransactionType type);
}
