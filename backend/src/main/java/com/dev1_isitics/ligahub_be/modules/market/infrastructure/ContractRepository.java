package com.dev1_isitics.ligahub_be.modules.market.infrastructure;

import com.dev1_isitics.ligahub_be.modules.market.domain.Contract;
import com.dev1_isitics.ligahub_be.modules.market.domain.ContractStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepository extends JpaRepository<Contract, Integer> {

  List<Contract> findByClubIdAndStatus(Integer clubId, ContractStatus status);

  Optional<Contract> findByPlayerIdAndStatus(Integer playerId, ContractStatus status);
}
