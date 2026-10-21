package com.dev1_isitics.ligahub_be.modules.players.domain;

import com.dev1_isitics.ligahub_be.shared.exception.ResourceNotFoundException;

/** Player that does not exist or was deleted (V8, RN09): 404. */
public class PlayerNotFoundException extends ResourceNotFoundException {

  private static final String MESSAGE = "Jogador %d não encontrado.";

  public PlayerNotFoundException(Integer playerId) {
    super(MESSAGE.formatted(playerId));
  }
}
