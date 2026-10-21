package com.dev1_isitics.ligahub_be.modules.league.infrastructure;

import com.dev1_isitics.ligahub_be.modules.league.domain.LineupEntry;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LineupEntryRepository extends JpaRepository<LineupEntry, LineupEntry.Key> {

  List<LineupEntry> findByClubId(Integer clubId);

  /** Runs right away, so the new starters can be inserted in the same transaction. */
  @Modifying
  @Query("delete from LineupEntry entry where entry.clubId = :clubId")
  void deleteByClubId(@Param("clubId") Integer clubId);

  /** A starter leaves the lineup when sold or when becoming DOENTE (RN19). */
  @Modifying
  @Query("delete from LineupEntry entry where entry.playerId = :playerId")
  void deleteByPlayerId(@Param("playerId") Integer playerId);
}
