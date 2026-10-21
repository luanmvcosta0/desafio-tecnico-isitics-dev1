package com.dev1_isitics.ligahub_be.modules.players.api;

import static com.dev1_isitics.ligahub_be.modules.players.domain.PlayerLimits.AGE_MAX;
import static com.dev1_isitics.ligahub_be.modules.players.domain.PlayerLimits.AGE_MIN;
import static com.dev1_isitics.ligahub_be.modules.players.domain.PlayerLimits.AGILITY_MAX;
import static com.dev1_isitics.ligahub_be.modules.players.domain.PlayerLimits.AGILITY_MIN;
import static com.dev1_isitics.ligahub_be.modules.players.domain.PlayerLimits.NAME_MAX_LENGTH;
import static com.dev1_isitics.ligahub_be.modules.players.domain.PlayerLimits.NAME_MIN_LENGTH;

import com.dev1_isitics.ligahub_be.modules.players.api.validation.AgeAndAgility;
import com.dev1_isitics.ligahub_be.modules.players.api.validation.AgilityAllowedForAge;
import com.dev1_isitics.ligahub_be.modules.players.api.validation.PlayerName;
import com.dev1_isitics.ligahub_be.modules.players.domain.Position;
import com.dev1_isitics.ligahub_be.shared.text.NormalizedText;
import com.dev1_isitics.ligahub_be.shared.validation.TextLength;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Body of {@code PUT /players/{id}}: the four fields are required even if only one changes (RN08).
 * Attributes, potential, base value and other fields are not declared and therefore ignored (V7).
 */
@AgilityAllowedForAge
public record PlayerUpdateRequest(
    @NotNull @NormalizedText @TextLength(min = NAME_MIN_LENGTH, max = NAME_MAX_LENGTH) @PlayerName
        String name,
    @NotNull Position position,
    @NotNull @Min(AGILITY_MIN) @Max(AGILITY_MAX) Integer agility,
    @NotNull @Min(AGE_MIN) @Max(AGE_MAX) Integer age)
    implements AgeAndAgility {}
