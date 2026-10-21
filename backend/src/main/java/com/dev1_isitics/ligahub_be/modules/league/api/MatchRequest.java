package com.dev1_isitics.ligahub_be.modules.league.api;

import com.dev1_isitics.ligahub_be.modules.league.api.validation.DifferentClubs;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** Body of {@code POST /matches}. */
@DifferentClubs
public record MatchRequest(
    @NotNull @Positive Integer homeClubId, @NotNull @Positive Integer awayClubId) {}
