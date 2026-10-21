package com.dev1_isitics.ligahub_be.modules.clubs.api;

import com.dev1_isitics.ligahub_be.modules.clubs.application.ClubService;
import com.dev1_isitics.ligahub_be.modules.clubs.domain.Club;
import com.dev1_isitics.ligahub_be.modules.clubs.domain.ClubValuation;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ClubController.CLUBS_PATH)
@RequiredArgsConstructor
public class ClubController {

  static final String CLUBS_PATH = "/api/v1/clubs";

  private final ClubService clubService;

  @PostMapping
  public ResponseEntity<ClubResponse> create(@Valid @RequestBody ClubCreateRequest request) {
    Club club = clubService.create(request);
    return ResponseEntity.created(URI.create(CLUBS_PATH + "/" + club.getId()))
        .body(ClubResponse.from(ClubValuation.of(club, List.of())));
  }
}
