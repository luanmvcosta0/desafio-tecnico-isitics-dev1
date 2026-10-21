package com.dev1_isitics.ligahub_be.modules.clubs.infrastructure;

import com.dev1_isitics.ligahub_be.modules.clubs.domain.Club;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ClubRepository extends JpaRepository<Club, Integer> {

  /** Same rule as the unique index {@code lower(nome)}: ignores letter case, keeps accents. */
  @Query("select count(club) > 0 from Club club where lower(club.name) = lower(:name)")
  boolean existsByNameIgnoringCase(@Param("name") String name);

  boolean existsByAcronym(String acronym);
}
