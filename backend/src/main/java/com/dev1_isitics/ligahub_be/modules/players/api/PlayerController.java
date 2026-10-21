package com.dev1_isitics.ligahub_be.modules.players.api;

import com.dev1_isitics.ligahub_be.modules.players.application.PlayerService;
import com.dev1_isitics.ligahub_be.modules.players.domain.Player;
import com.dev1_isitics.ligahub_be.shared.web.PathIds;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(PlayerController.PLAYERS_PATH)
@RequiredArgsConstructor
public class PlayerController {

  static final String PLAYERS_PATH = "/api/v1/players";

  private final PlayerService playerService;

  @PostMapping
  public ResponseEntity<PlayerResponse> register(@Valid @RequestBody PlayerCreateRequest request) {
    Player player = playerService.register(request);
    return ResponseEntity.created(URI.create(PLAYERS_PATH + "/" + player.getId()))
        .body(PlayerResponse.from(player));
  }

  @GetMapping("/" + PathIds.ID)
  public PlayerResponse find(@PathVariable Integer id) {
    return PlayerResponse.from(playerService.findActive(id));
  }

  @PutMapping("/" + PathIds.ID)
  public PlayerResponse edit(
      @PathVariable Integer id, @Valid @RequestBody PlayerUpdateRequest request) {
    return PlayerResponse.from(playerService.edit(id, request));
  }

  @PatchMapping("/" + PathIds.ID + "/condition")
  public PlayerResponse changeCondition(
      @PathVariable Integer id, @Valid @RequestBody ConditionChangeRequest request) {
    return PlayerResponse.from(playerService.changeCondition(id, request.condition()));
  }

  @DeleteMapping("/" + PathIds.ID)
  public ResponseEntity<Void> delete(@PathVariable Integer id) {
    playerService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
