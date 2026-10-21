package com.dev1_isitics.ligahub_be.modules.clubs.api;

import com.dev1_isitics.ligahub_be.modules.clubs.application.ClubQueryService;
import com.dev1_isitics.ligahub_be.modules.clubs.application.ClubService;
import com.dev1_isitics.ligahub_be.modules.clubs.application.ClubSort;
import com.dev1_isitics.ligahub_be.modules.clubs.domain.ClubValuation;
import com.dev1_isitics.ligahub_be.shared.web.PathIds;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
  private final ClubQueryService clubQueryService;

  @GetMapping
  public List<ClubResponse> list(ClubSort sort) {
    return clubQueryService.list(sort).stream().map(ClubResponse::from).toList();
  }

  @PostMapping
  public ResponseEntity<ClubResponse> create(@Valid @RequestBody ClubCreateRequest request) {
    ClubValuation created = clubService.create(request);
    return ResponseEntity.created(URI.create(CLUBS_PATH + "/" + created.club().getId()))
        .body(ClubResponse.from(created));
  }

  @GetMapping("/" + PathIds.ID)
  public ClubDetailsResponse find(@PathVariable Integer id) {
    return ClubDetailsResponse.from(clubQueryService.find(id));
  }
}
