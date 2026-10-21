package com.dev1_isitics.ligahub_be.modules.clubs.application;

import com.dev1_isitics.ligahub_be.modules.clubs.domain.Club;
import com.dev1_isitics.ligahub_be.modules.clubs.domain.ClubValuation;
import com.dev1_isitics.ligahub_be.modules.clubs.infrastructure.ClubRepository;
import com.dev1_isitics.ligahub_be.modules.players.application.PlayerSort;
import com.dev1_isitics.ligahub_be.modules.players.domain.Player;
import com.dev1_isitics.ligahub_be.modules.players.infrastructure.PlayerRepository;
import com.dev1_isitics.ligahub_be.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Clubs with their squads, squad value and net worth, computed on every read (RN11). */
@Service
@RequiredArgsConstructor
public class ClubQueryService {

  private static final String CLUB_NOT_FOUND_MESSAGE = "Clube %d não encontrado.";

  private final ClubRepository clubRepository;
  private final PlayerRepository playerRepository;

  @Transactional(readOnly = true)
  public List<ClubValuation> list(ClubSort sort) {
    List<Club> clubs = clubRepository.findAll();
    Map<Integer, List<Player>> squads =
        playerRepository
            .findByClubIdInAndDeletedAtIsNull(clubs.stream().map(Club::getId).toList())
            .stream()
            .collect(Collectors.groupingBy(Player::getClubId));
    return clubs.stream()
        .map(club -> valuate(club, squads.getOrDefault(club.getId(), List.of())))
        .sorted(sort.withTieBreak())
        .toList();
  }

  @Transactional(readOnly = true)
  public ClubValuation find(Integer id) {
    Club club =
        clubRepository
            .findById(id)
            .orElseThrow(() -> new ResourceNotFoundException(CLUB_NOT_FOUND_MESSAGE.formatted(id)));
    return valuate(club, playerRepository.findByClubIdInAndDeletedAtIsNull(List.of(id)));
  }

  private static ClubValuation valuate(Club club, List<Player> squad) {
    return ClubValuation.of(club, squad.stream().sorted(PlayerSort.SQUAD_ORDER).toList());
  }
}
