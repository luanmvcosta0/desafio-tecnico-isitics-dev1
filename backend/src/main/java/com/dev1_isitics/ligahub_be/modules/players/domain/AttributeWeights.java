package com.dev1_isitics.ligahub_be.modules.players.domain;

/** Weight (%) of each technical attribute in the overall of a position (RN06); they sum to 100. */
public record AttributeWeights(
    int pace, int shooting, int passing, int dribbling, int defending, int physical) {}
