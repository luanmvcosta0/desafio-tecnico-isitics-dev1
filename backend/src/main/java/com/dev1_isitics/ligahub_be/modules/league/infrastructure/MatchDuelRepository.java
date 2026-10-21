package com.dev1_isitics.ligahub_be.modules.league.infrastructure;

import com.dev1_isitics.ligahub_be.modules.league.domain.MatchDuel;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MatchDuelRepository extends JpaRepository<MatchDuel, Integer> {

  List<MatchDuel> findByMatchIdOrderByOrderAsc(Integer matchId);
}
