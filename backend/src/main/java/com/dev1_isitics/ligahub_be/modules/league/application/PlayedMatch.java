package com.dev1_isitics.ligahub_be.modules.league.application;

import com.dev1_isitics.ligahub_be.modules.clubs.domain.Club;
import com.dev1_isitics.ligahub_be.modules.league.domain.Match;
import com.dev1_isitics.ligahub_be.modules.league.domain.MatchDuel;
import java.util.List;

/** A recorded match with its clubs and, when requested, its six duels in order. */
public record PlayedMatch(Match match, Club home, Club away, List<MatchDuel> duels) {}
