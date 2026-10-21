package com.dev1_isitics.ligahub_be.modules.league.application;

import com.dev1_isitics.ligahub_be.modules.clubs.domain.Club;
import com.dev1_isitics.ligahub_be.modules.clubs.domain.ClubNotFoundException;
import com.dev1_isitics.ligahub_be.modules.clubs.infrastructure.ClubRepository;
import com.dev1_isitics.ligahub_be.modules.league.domain.Match;
import com.dev1_isitics.ligahub_be.modules.league.domain.Standing;
import com.dev1_isitics.ligahub_be.modules.league.domain.StandingsCalculator;
import com.dev1_isitics.ligahub_be.modules.league.infrastructure.MatchDuelRepository;
import com.dev1_isitics.ligahub_be.modules.league.infrastructure.MatchRepository;
import com.dev1_isitics.ligahub_be.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Match history, match details and standings, all read as recorded (RN21). */
@Service
@RequiredArgsConstructor
public class MatchQueryService {

  private static final String MATCH_NOT_FOUND_MESSAGE = "Partida %d não encontrada.";

  private final ClubRepository clubRepository;
  private final MatchRepository matchRepository;
  private final MatchDuelRepository matchDuelRepository;

  @Transactional(readOnly = true)
  public List<PlayedMatch> history(Integer clubId) {
    if (clubId != null && !clubRepository.existsById(clubId)) {
      throw new ClubNotFoundException(clubId);
    }
    List<Match> matches =
        clubId == null
            ? matchRepository.findAllByOrderByPlayedAtDescIdDesc()
            : matchRepository.findByClub(clubId);
    Map<Integer, Club> clubs = clubsById();
    return matches.stream()
        .map(
            match ->
                new PlayedMatch(
                    match,
                    clubs.get(match.getHomeClubId()),
                    clubs.get(match.getAwayClubId()),
                    List.of()))
        .toList();
  }

  @Transactional(readOnly = true)
  public PlayedMatch find(Integer matchId) {
    Match match =
        matchRepository
            .findById(matchId)
            .orElseThrow(
                () -> new ResourceNotFoundException(MATCH_NOT_FOUND_MESSAGE.formatted(matchId)));
    Map<Integer, Club> clubs = clubsById();
    return new PlayedMatch(
        match,
        clubs.get(match.getHomeClubId()),
        clubs.get(match.getAwayClubId()),
        matchDuelRepository.findByMatchIdOrderByOrderAsc(matchId));
  }

  @Transactional(readOnly = true)
  public List<Standing> standings() {
    return StandingsCalculator.compute(clubRepository.findAll(), matchRepository.findAll());
  }

  private Map<Integer, Club> clubsById() {
    return clubRepository.findAll().stream()
        .collect(Collectors.toMap(Club::getId, Function.identity()));
  }
}
