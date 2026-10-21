package com.dev1_isitics.ligahub_be.modules.league.api;

import com.dev1_isitics.ligahub_be.modules.league.application.LineupService;
import com.dev1_isitics.ligahub_be.shared.web.PathIds;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clubs/" + PathIds.ID + "/lineup")
@RequiredArgsConstructor
public class LineupController {

  private final LineupService lineupService;

  @PutMapping
  public LineupResponse save(@PathVariable Integer id, @Valid @RequestBody LineupRequest request) {
    return LineupResponse.from(lineupService.save(id, request.formation(), request.playerIds()));
  }

  @GetMapping
  public LineupResponse find(@PathVariable Integer id) {
    return LineupResponse.from(lineupService.find(id));
  }
}
