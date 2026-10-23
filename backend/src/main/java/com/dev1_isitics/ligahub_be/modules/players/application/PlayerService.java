package com.dev1_isitics.ligahub_be.modules.players.application;

import com.dev1_isitics.ligahub_be.modules.league.infrastructure.LineupEntryRepository;
import com.dev1_isitics.ligahub_be.modules.players.api.PlayerCreateRequest;
import com.dev1_isitics.ligahub_be.modules.players.api.PlayerUpdateRequest;
import com.dev1_isitics.ligahub_be.modules.players.domain.Condition;
import com.dev1_isitics.ligahub_be.modules.players.domain.Player;
import com.dev1_isitics.ligahub_be.modules.players.domain.PlayerNotFoundException;
import com.dev1_isitics.ligahub_be.modules.players.infrastructure.PlayerRepository;
import com.dev1_isitics.ligahub_be.shared.exception.BusinessRuleException;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerService {

  private static final String POSITION_LOCKED_MESSAGE =
      "Jogador com clube não pode mudar de posição (atual: %s).";
  private static final String AGE_DECREASE_MESSAGE =
      "A idade não pode ser menor que a atual (%d anos).";
  private static final String SICK_TO_FIT_MESSAGE =
      "Jogador DOENTE não pode passar direto para APTO; passe antes por MAL_CONDICIONADO.";
  private static final String DELETE_WITH_CLUB_MESSAGE =
      "Só jogador livre pode ser excluído; venda o jogador antes de excluí-lo.";

  private final PlayerRepository playerRepository;
  private final LineupEntryRepository lineupEntryRepository;
  private final Clock clock;

  @Transactional
  public Player register(PlayerCreateRequest request) {
    Player player =
        Player.register(
            request.name(),
            request.position(),
            request.agility(),
            request.age(),
            request.toAttributes(),
            request.potential());
    return playerRepository.save(player);
  }

  @Transactional(readOnly = true)
  public Player findActive(Integer id) {
    return playerRepository
        .findByIdAndDeletedAtIsNull(id)
        .orElseThrow(() -> new PlayerNotFoundException(id));
  }

  @Transactional
  public Player edit(Integer id, PlayerUpdateRequest request) {
    Player player = findActiveForUpdate(id);
    if (!player.isFree() && player.getPosition() != request.position()) {
      throw new BusinessRuleException(POSITION_LOCKED_MESSAGE.formatted(player.getPosition()));
    }
    if (request.age() < player.getAge()) {
      throw new BusinessRuleException(AGE_DECREASE_MESSAGE.formatted(player.getAge()));
    }
    player.edit(request.name(), request.position(), request.agility(), request.age());
    return player;
  }

  /** A starter that becomes DOENTE leaves the lineup in the same transaction (RN19). */
  @Transactional
  public Player changeCondition(Integer id, Condition condition) {
    Player player = findActiveForUpdate(id);
    if (player.getCondition() == Condition.DOENTE && condition == Condition.APTO) {
      throw new BusinessRuleException(SICK_TO_FIT_MESSAGE);
    }
    player.changeCondition(condition);
    if (condition == Condition.DOENTE) {
      lineupEntryRepository.deleteByPlayerId(player.getId());
    }
    return player;
  }

  @Transactional
  public void delete(Integer id) {
    Player player = findActiveForUpdate(id);
    if (!player.isFree()) {
      throw new BusinessRuleException(DELETE_WITH_CLUB_MESSAGE);
    }
    player.delete(clock.instant());
  }

  /**
   * Waits for a concurrent purchase or sale of the player, so the checks see its current club and
   * the update does not write back a stale {@code clube_id}.
   */
  private Player findActiveForUpdate(Integer id) {
    return playerRepository
        .findActiveByIdForUpdate(id)
        .orElseThrow(() -> new PlayerNotFoundException(id));
  }
}
