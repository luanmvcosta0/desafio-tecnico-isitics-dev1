package com.dev1_isitics.ligahub_be.modules.players.api;

import static com.dev1_isitics.ligahub_be.modules.players.domain.PlayerLimits.AGE_MAX;
import static com.dev1_isitics.ligahub_be.modules.players.domain.PlayerLimits.AGE_MIN;
import static com.dev1_isitics.ligahub_be.modules.players.domain.PlayerLimits.AGILITY_MAX;
import static com.dev1_isitics.ligahub_be.modules.players.domain.PlayerLimits.AGILITY_MIN;
import static com.dev1_isitics.ligahub_be.modules.players.domain.PlayerLimits.ATTRIBUTE_MAX;
import static com.dev1_isitics.ligahub_be.modules.players.domain.PlayerLimits.ATTRIBUTE_MIN;
import static com.dev1_isitics.ligahub_be.modules.players.domain.PlayerLimits.NAME_MAX_LENGTH;
import static com.dev1_isitics.ligahub_be.modules.players.domain.PlayerLimits.NAME_MIN_LENGTH;

import com.dev1_isitics.ligahub_be.modules.players.api.validation.AgeAndAgility;
import com.dev1_isitics.ligahub_be.modules.players.api.validation.AgilityAllowedForAge;
import com.dev1_isitics.ligahub_be.modules.players.api.validation.ConsistentTechnicalAttributes;
import com.dev1_isitics.ligahub_be.modules.players.api.validation.PlayerName;
import com.dev1_isitics.ligahub_be.modules.players.domain.Position;
import com.dev1_isitics.ligahub_be.modules.players.domain.TechnicalAttributes;
import com.dev1_isitics.ligahub_be.shared.text.NormalizedText;
import com.dev1_isitics.ligahub_be.shared.validation.TextLength;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Body of {@code POST /players}. Fields controlled by the system ({@code id}, {@code condition},
 * {@code club_id}, {@code base_value}...) are not declared and therefore ignored (V7).
 */
@AgilityAllowedForAge
@ConsistentTechnicalAttributes
public record PlayerCreateRequest(
    @NotNull @NormalizedText @TextLength(min = NAME_MIN_LENGTH, max = NAME_MAX_LENGTH) @PlayerName
        String name,
    @NotNull Position position,
    @NotNull @Min(AGILITY_MIN) @Max(AGILITY_MAX) Integer agility,
    @NotNull @Min(AGE_MIN) @Max(AGE_MAX) Integer age,
    @NotNull @Min(ATTRIBUTE_MIN) @Max(ATTRIBUTE_MAX) Integer pace,
    @NotNull @Min(ATTRIBUTE_MIN) @Max(ATTRIBUTE_MAX) Integer shooting,
    @NotNull @Min(ATTRIBUTE_MIN) @Max(ATTRIBUTE_MAX) Integer passing,
    @NotNull @Min(ATTRIBUTE_MIN) @Max(ATTRIBUTE_MAX) Integer dribbling,
    @NotNull @Min(ATTRIBUTE_MIN) @Max(ATTRIBUTE_MAX) Integer defending,
    @NotNull @Min(ATTRIBUTE_MIN) @Max(ATTRIBUTE_MAX) Integer physical,
    @NotNull @Min(ATTRIBUTE_MIN) @Max(ATTRIBUTE_MAX) Integer potential)
    implements AgeAndAgility {

  public TechnicalAttributes toAttributes() {
    return new TechnicalAttributes(pace, shooting, passing, dribbling, defending, physical);
  }
}
