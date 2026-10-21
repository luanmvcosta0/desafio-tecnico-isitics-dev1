package com.dev1_isitics.ligahub_be.modules.league.api;

import com.dev1_isitics.ligahub_be.modules.league.application.MatchQueryService;
import com.dev1_isitics.ligahub_be.modules.league.application.MatchService;
import com.dev1_isitics.ligahub_be.modules.league.application.PlayedMatch;
import com.dev1_isitics.ligahub_be.shared.web.PathIds;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MatchController {

  private static final String MATCHES_PATH = "/api/v1/matches";

  private final MatchService matchService;
  private final MatchQueryService matchQueryService;

  @PostMapping(MATCHES_PATH)
  public ResponseEntity<MatchResponse> play(@Valid @RequestBody MatchRequest request) {
    PlayedMatch played = matchService.play(request.homeClubId(), request.awayClubId());
    return ResponseEntity.created(URI.create(MATCHES_PATH + "/" + played.match().getId()))
        .body(MatchResponse.withDuels(played));
  }

  @GetMapping(MATCHES_PATH + "/" + PathIds.ID)
  public MatchResponse find(@PathVariable Integer id) {
    return MatchResponse.withDuels(matchQueryService.find(id));
  }

}
