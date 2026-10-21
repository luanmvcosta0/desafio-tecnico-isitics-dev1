package com.dev1_isitics.ligahub_be.modules.players.infrastructure;

import com.dev1_isitics.ligahub_be.modules.players.domain.Player;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerRepository extends JpaRepository<Player, Integer>, JpaSpecificationExecutor<Player> {

  /** Deleted players count as nonexistent everywhere (RN09). */
  Optional<Player> findByIdAndDeletedAtIsNull(Integer id);

  List<Player> findByClubIdInAndDeletedAtIsNull(Collection<Integer> clubIds);

  long countByClubIdAndDeletedAtIsNull(Integer clubId);

  /** Locks the player row so two clubs cannot buy the same player at the same time. */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select player from Player player where player.id = :id and player.deletedAt is null")
  Optional<Player> findActiveByIdForUpdate(@Param("id") Integer id);
}
