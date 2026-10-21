package com.dev1_isitics.ligahub_be.modules.players.infrastructure;

import com.dev1_isitics.ligahub_be.modules.players.domain.Player;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PlayerRepository extends JpaRepository<Player, Integer>, JpaSpecificationExecutor<Player> {

  /** Deleted players count as nonexistent everywhere (RN09). */
  Optional<Player> findByIdAndDeletedAtIsNull(Integer id);
}
