package com.dev1_isitics.ligahub_be.modules.players.infrastructure;

import com.dev1_isitics.ligahub_be.modules.players.domain.Player;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlayerRepository extends JpaRepository<Player, Integer> {

  /** Deleted players count as nonexistent everywhere (RN09). */
  Optional<Player> findByIdAndDeletedAtIsNull(Integer id);
}
