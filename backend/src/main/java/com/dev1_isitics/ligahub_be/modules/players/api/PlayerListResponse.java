package com.dev1_isitics.ligahub_be.modules.players.api;

import com.dev1_isitics.ligahub_be.modules.players.application.PlayerPage;
import java.util.List;

/** {@code GET /players}: one page in {@code data} and the totals in {@code meta} (RN07). */
public record PlayerListResponse(List<PlayerResponse> data, Meta meta) {

  public record Meta(int page, int limit, long totalItems, long totalPages) {}

  public static PlayerListResponse from(PlayerPage page) {
    return new PlayerListResponse(
        page.items().stream().map(PlayerResponse::from).toList(),
        new Meta(page.page(), page.limit(), page.totalItems(), page.totalPages()));
  }
}
