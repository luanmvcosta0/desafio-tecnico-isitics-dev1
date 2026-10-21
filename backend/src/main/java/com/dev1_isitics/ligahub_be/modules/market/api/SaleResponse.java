package com.dev1_isitics.ligahub_be.modules.market.api;

import com.dev1_isitics.ligahub_be.modules.market.application.Sale;
import com.dev1_isitics.ligahub_be.modules.market.domain.ContractStatus;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.math.BigDecimal;

/** {@code POST /players/{id}/sell}: the VENDA transaction plus contract and penalty details. */
public record SaleResponse(
    @JsonUnwrapped TransactionResponse transaction,
    BigDecimal purchaseValue,
    long remainingDays,
    int penaltyRate,
    ContractStatus contractStatus,
    BigDecimal clubCash) {

  public static SaleResponse from(Sale sale) {
    return new SaleResponse(
        TransactionResponse.from(sale.transaction()),
        sale.contract().getPurchaseValue(),
        sale.remainingDays(),
        sale.penaltyRate(),
        sale.contract().getStatus(),
        sale.clubCash());
  }
}
