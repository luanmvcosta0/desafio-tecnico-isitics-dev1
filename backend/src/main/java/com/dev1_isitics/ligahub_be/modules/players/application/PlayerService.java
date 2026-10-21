package com.dev1_isitics.ligahub_be.modules.players.application;

import com.dev1_isitics.ligahub_be.modules.players.api.PlayerCreateRequest;
import com.dev1_isitics.ligahub_be.modules.players.domain.Player;
import com.dev1_isitics.ligahub_be.modules.players.infrastructure.PlayerRepository;
import com.dev1_isitics.ligahub_be.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlayerService {

  private static final String PLAYER_NOT_FOUND_MESSAGE = "Jogador %d não encontrado.";

  private final PlayerRepository playerRepository;

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
        .orElseThrow(() -> new ResourceNotFoundException(PLAYER_NOT_FOUND_MESSAGE.formatted(id)));
  }
}
