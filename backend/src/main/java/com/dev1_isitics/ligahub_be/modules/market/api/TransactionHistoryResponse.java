package com.dev1_isitics.ligahub_be.modules.market.api;

import com.dev1_isitics.ligahub_be.modules.market.application.TransactionHistory;
import com.dev1_isitics.ligahub_be.modules.market.domain.TransactionSummary;
import java.util.List;

/** {@code GET /clubs/{id}/transactions}: the transactions and the totals, both filtered. */
public record TransactionHistoryResponse(
    List<TransactionResponse> data, TransactionSummary summary) {

  public static TransactionHistoryResponse from(TransactionHistory history) {
    return new TransactionHistoryResponse(
        history.transactions().stream().map(TransactionResponse::from).toList(), history.summary());
  }
}
