package com.dev1_isitics.ligahub_be.modules.players.application;

import com.dev1_isitics.ligahub_be.modules.players.domain.Condition;
import com.dev1_isitics.ligahub_be.modules.players.domain.Position;
import java.math.BigDecimal;
import java.util.List;

/**
 * Validated filters, sorting and page of {@code GET /players}. Absent optional filters are {@code
 * null}; ranges already hold their extremes when only one limit was informed.
 */
public record PlayerListQuery(
    String search,
    Position position,
    Condition condition,
    Boolean free,
    Integer clubId,
    int minAge,
    int maxAge,
    BigDecimal minValue,
    BigDecimal maxValue,
    List<PlayerSort> sorts,
    int page,
    int limit) {}
