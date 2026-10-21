package com.dev1_isitics.ligahub_be.modules.market.infrastructure;

import com.dev1_isitics.ligahub_be.modules.market.domain.Transaction;
import com.dev1_isitics.ligahub_be.modules.market.domain.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TransactionRepository
    extends JpaRepository<Transaction, Integer>, JpaSpecificationExecutor<Transaction> {

  boolean existsByClubIdAndPlayerIdAndType(Integer clubId, Integer playerId, TransactionType type);
}
