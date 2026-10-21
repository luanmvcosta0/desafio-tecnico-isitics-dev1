package com.dev1_isitics.ligahub_be.modules.league.application;

import com.dev1_isitics.ligahub_be.modules.clubs.domain.Club;
import com.dev1_isitics.ligahub_be.modules.clubs.infrastructure.ClubRepository;
import com.dev1_isitics.ligahub_be.modules.league.domain.DuelResult;
import com.dev1_isitics.ligahub_be.modules.league.domain.Match;
import com.dev1_isitics.ligahub_be.modules.league.domain.MatchDuel;
import com.dev1_isitics.ligahub_be.modules.league.domain.MatchEngine;
import com.dev1_isitics.ligahub_be.modules.league.domain.TeamSheet;
import com.dev1_isitics.ligahub_be.modules.league.infrastructure.MatchDuelRepository;
import com.dev1_isitics.ligahub_be.modules.league.infrastructure.MatchRepository;
import com.dev1_isitics.ligahub_be.shared.exception.BusinessRuleException;
import com.dev1_isitics.ligahub_be.shared.exception.ResourceNotFoundException;
import java.time.Clock;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Plays a match on the spot (RN20) after the checks 404 (clubs) and 409 (complete lineups), and
 * records the match and its six duels in a single transaction. It changes nothing else.
 */
@Service
@RequiredArgsConstructor
public class MatchService {

  private static final String CLUB_NOT_FOUND_MESSAGE = "Clube %d não encontrado.";
  private static final String INCOMPLETE_LINEUP_MESSAGE =
      "O clube %s não tem escalação completa (5 titulares) e não pode jogar.";

  private final ClubRepository clubRepository;
  private final LineupService lineupService;
  private final MatchRepository matchRepository;
  private final MatchDuelRepository matchDuelRepository;
  private final Clock clock;

  @Transactional
  public PlayedMatch play(Integer homeClubId, Integer awayClubId) {
    Club home = findClub(homeClubId);
    Club away = findClub(awayClubId);
    TeamSheet homeSheet = teamSheetOf(home);
    TeamSheet awaySheet = teamSheetOf(away);

    List<DuelResult> duels = MatchEngine.play(homeSheet, awaySheet);
    Match match =
        matchRepository.save(
            Match.record(
                homeSheet,
                awaySheet,
                MatchEngine.goalsOf(home.getId(), duels),
                MatchEngine.goalsOf(away.getId(), duels),
                clock.instant()));
    List<MatchDuel> recorded =
        matchDuelRepository.saveAll(
            duels.stream().map(duel -> MatchDuel.record(match.getId(), duel)).toList());
    return new PlayedMatch(match, home, away, recorded);
  }

  private TeamSheet teamSheetOf(Club club) {
    Lineup lineup = lineupService.find(club.getId());
    if (!lineup.isComplete()) {
      throw new BusinessRuleException(INCOMPLETE_LINEUP_MESSAGE.formatted(club.getName()));
    }
    return new TeamSheet(club.getId(), lineup.formation(), lineup.starters());
  }

  private Club findClub(Integer clubId) {
    return clubRepository
        .findById(clubId)
        .orElseThrow(() -> new ResourceNotFoundException(CLUB_NOT_FOUND_MESSAGE.formatted(clubId)));
  }
}
