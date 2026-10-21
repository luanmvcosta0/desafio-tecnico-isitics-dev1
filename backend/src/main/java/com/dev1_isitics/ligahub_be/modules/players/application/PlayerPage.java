package com.dev1_isitics.ligahub_be.modules.players.application;

import com.dev1_isitics.ligahub_be.modules.players.domain.Player;
import java.util.List;

/** One page of the listing plus the totals of every player matching the filters (RN07). */
public record PlayerPage(
    List<Player> items, int page, int limit, long totalItems, long totalPages) {}
