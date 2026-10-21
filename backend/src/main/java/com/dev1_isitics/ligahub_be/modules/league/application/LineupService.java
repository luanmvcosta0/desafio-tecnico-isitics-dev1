package com.dev1_isitics.ligahub_be.modules.league.application;

import com.dev1_isitics.ligahub_be.modules.clubs.domain.Club;
import com.dev1_isitics.ligahub_be.modules.clubs.domain.ClubNotFoundException;
import com.dev1_isitics.ligahub_be.modules.clubs.infrastructure.ClubRepository;
import com.dev1_isitics.ligahub_be.modules.league.domain.Formation;
import com.dev1_isitics.ligahub_be.modules.league.domain.LineupEntry;
import com.dev1_isitics.ligahub_be.modules.league.infrastructure.LineupEntryRepository;
import com.dev1_isitics.ligahub_be.modules.players.application.PlayerSort;
import com.dev1_isitics.ligahub_be.modules.players.domain.Condition;
import com.dev1_isitics.ligahub_be.modules.players.domain.Player;
import com.dev1_isitics.ligahub_be.modules.players.domain.PlayerNotFoundException;
import com.dev1_isitics.ligahub_be.modules.players.infrastructure.PlayerRepository;
import com.dev1_isitics.ligahub_be.shared.exception.BusinessRuleException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lineup of a club (RN19). Saving replaces the whole lineup in a single transaction, after the
 * checks 404 (club, players) and 409 (squad, DOENTE, formation). It never changes the cash.
 */
@Service
@RequiredArgsConstructor
public class LineupService {

  private static final String NOT_IN_SQUAD_MESSAGE =
      "O jogador %s não pertence ao elenco do clube %s.";
  private static final String SICK_PLAYER_MESSAGE =
      "O jogador %s está DOENTE e não pode ser escalado.";
  private static final String FORMATION_MISMATCH_MESSAGE =
      "As posições dos titulares não fecham a formação %s: são necessários 1 GOL e os jogadores de"
          + " linha de cada setor.";

  private final ClubRepository clubRepository;
  private final PlayerRepository playerRepository;
  private final LineupEntryRepository lineupEntryRepository;
  private final Clock clock;

  @Transactional
  public Lineup save(Integer clubId, Formation formation, List<Integer> playerIds) {
    Club club = findClub(clubId);
    List<Player> starters = playerIds.stream().map(this::findPlayer).toList();
    for (Player starter : starters) {
      if (!club.getId().equals(starter.getClubId())) {
        throw new BusinessRuleException(
            NOT_IN_SQUAD_MESSAGE.formatted(starter.getName(), club.getName()));
      }
      if (starter.getCondition() == Condition.DOENTE) {
        throw new BusinessRuleException(SICK_PLAYER_MESSAGE.formatted(starter.getName()));
      }
    }
    if (!formation.isClosedBy(starters.stream().map(Player::getPosition).toList())) {
      throw new BusinessRuleException(FORMATION_MISMATCH_MESSAGE.formatted(formation.code()));
    }

    club.changeFormation(formation);
    lineupEntryRepository.deleteByClubId(clubId);
    Instant now = clock.instant();
    lineupEntryRepository.saveAll(
        starters.stream().map(starter -> new LineupEntry(clubId, starter.getId(), now)).toList());
    return new Lineup(clubId, formation, inSquadOrder(starters));
  }

  @Transactional(readOnly = true)
  public Lineup find(Integer clubId) {
    Club club = findClub(clubId);
    List<Integer> starterIds =
        lineupEntryRepository.findByClubId(clubId).stream().map(LineupEntry::getPlayerId).toList();
    return new Lineup(
        clubId, club.getFormation(), inSquadOrder(playerRepository.findAllById(starterIds)));
  }

  private Club findClub(Integer clubId) {
    return clubRepository.findById(clubId).orElseThrow(() -> new ClubNotFoundException(clubId));
  }

  private Player findPlayer(Integer playerId) {
    return playerRepository
        .findByIdAndDeletedAtIsNull(playerId)
        .orElseThrow(() -> new PlayerNotFoundException(playerId));
  }

  private static List<Player> inSquadOrder(List<Player> players) {
    return players.stream().sorted(PlayerSort.SQUAD_ORDER).toList();
  }
}
