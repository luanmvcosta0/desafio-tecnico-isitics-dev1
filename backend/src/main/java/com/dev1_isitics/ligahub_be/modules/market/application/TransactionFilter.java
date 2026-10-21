package com.dev1_isitics.ligahub_be.modules.market.application;

import com.dev1_isitics.ligahub_be.modules.market.domain.TransactionType;
import java.util.Set;

/** Validated filters of the history and of its CSV export; no type means every type (RN17). */
public record TransactionFilter(Set<TransactionType> types, Integer playerId) {}
