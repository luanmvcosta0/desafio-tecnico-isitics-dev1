package com.dev1_isitics.ligahub_be.modules.clubs.infrastructure;

import com.dev1_isitics.ligahub_be.modules.clubs.domain.Club;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClubRepository extends JpaRepository<Club, Integer> {

  /** Same rule as the unique index {@code lower(nome)}: ignores letter case, keeps accents. */
  @Query("select count(club) > 0 from Club club where lower(club.name) = lower(:name)")
  boolean existsByNameIgnoringCase(@Param("name") String name);

  boolean existsByAcronym(String acronym);

  /**
   * Locks the club row so concurrent purchases and sales see the cash already changed, and saves of
   * its lineup do not overlap.
   */
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select club from Club club where club.id = :id")
  Optional<Club> findByIdForUpdate(@Param("id") Integer id);
}
