package com.dev1_isitics.ligahub_be.modules.clubs.application;

import com.dev1_isitics.ligahub_be.modules.clubs.domain.ClubValuation;
import java.util.List;

/** Club of {@code GET /clubs/{id}}: values plus the squad members in the squad order. */
public record ClubDetails(ClubValuation valuation, List<SquadMember> squad) {}
