package com.dev1_isitics.ligahub_be.modules.league.api;

/** Optional {@code club_id} of {@code GET /matches}, already validated (null = every match). */
public record MatchClubFilter(Integer clubId) {}
