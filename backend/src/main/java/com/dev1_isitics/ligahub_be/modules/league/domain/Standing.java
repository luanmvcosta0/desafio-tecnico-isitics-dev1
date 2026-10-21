package com.dev1_isitics.ligahub_be.modules.league.domain;

import com.dev1_isitics.ligahub_be.modules.clubs.domain.Club;

/** Row of the standings, computed on every read and never stored (RN21). */
public record Standing(
    int position,
    Club club,
    int played,
    int wins,
    int draws,
    int losses,
    int goalsFor,
    int goalsAgainst,
    int goalDifference,
    int points) {}
