package com.dev1_isitics.ligahub_be.modules.players.api;

import com.dev1_isitics.ligahub_be.modules.players.domain.Condition;
import jakarta.validation.constraints.NotNull;

/** Body of {@code PATCH /players/{id}/condition}; any other field is ignored (V7). */
public record ConditionChangeRequest(@NotNull Condition condition) {}
