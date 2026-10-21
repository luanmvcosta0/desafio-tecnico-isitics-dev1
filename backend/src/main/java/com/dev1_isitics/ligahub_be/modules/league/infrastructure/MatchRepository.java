package com.dev1_isitics.ligahub_be.modules.league.infrastructure;

import com.dev1_isitics.ligahub_be.modules.league.domain.Match;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MatchRepository extends JpaRepository<Match, Integer> {

  /** Most recent first; same instant, higher id first (RN21). */
  List<Match> findAllByOrderByPlayedAtDescIdDesc();

  @Query(
      "select match from Match match where match.homeClubId = :clubId or match.awayClubId = :clubId"
          + " order by match.playedAt desc, match.id desc")
  List<Match> findByClub(@Param("clubId") Integer clubId);
}
