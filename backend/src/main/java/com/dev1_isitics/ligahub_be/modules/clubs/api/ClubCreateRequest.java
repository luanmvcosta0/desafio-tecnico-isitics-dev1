package com.dev1_isitics.ligahub_be.modules.clubs.api;

import static com.dev1_isitics.ligahub_be.modules.clubs.domain.ClubLimits.ACRONYM_PATTERN;
import static com.dev1_isitics.ligahub_be.modules.clubs.domain.ClubLimits.NAME_MAX_LENGTH;
import static com.dev1_isitics.ligahub_be.modules.clubs.domain.ClubLimits.NAME_MIN_LENGTH;

import com.dev1_isitics.ligahub_be.modules.clubs.api.validation.AcronymMatchesName;
import com.dev1_isitics.ligahub_be.modules.clubs.api.validation.ClubName;
import com.dev1_isitics.ligahub_be.shared.text.NormalizedText;
import com.dev1_isitics.ligahub_be.shared.validation.TextLength;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/** Body of {@code POST /clubs}; {@code cash} and other fields are ignored (V3, V7). */
@AcronymMatchesName
public record ClubCreateRequest(
    @NotNull @NormalizedText @TextLength(min = NAME_MIN_LENGTH, max = NAME_MAX_LENGTH) @ClubName
        String name,
    @NotNull
        @Pattern(
            regexp = ACRONYM_PATTERN,
            message = "deve ter exatamente 3 letras de A a Z maiúsculas")
        String acronym) {}
