package com.dev1_isitics.ligahub_be.modules.league.api;

import com.dev1_isitics.ligahub_be.modules.league.domain.Formation;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.hibernate.validator.constraints.UniqueElements;

/** Body of {@code PUT /clubs/{id}/lineup}: the formation and exactly 5 distinct player ids. */
public record LineupRequest(
    @NotNull Formation formation,
    @NotNull
        @Size(
            min = Formation.STARTERS,
            max = Formation.STARTERS,
            message = "deve ter exatamente 5 ids")
        @UniqueElements(message = "não pode repetir jogadores")
        List<@NotNull @Positive Integer> playerIds) {}
