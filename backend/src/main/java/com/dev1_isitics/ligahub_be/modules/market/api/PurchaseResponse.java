package com.dev1_isitics.ligahub_be.modules.market.api;

import com.dev1_isitics.ligahub_be.modules.market.application.Purchase;
import com.dev1_isitics.ligahub_be.modules.market.domain.Contract;
import com.dev1_isitics.ligahub_be.modules.market.domain.ContractStatus;
import com.dev1_isitics.ligahub_be.shared.time.BrasiliaDateTime;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.math.BigDecimal;

/** {@code POST /players/{id}/buy}: the COMPRA transaction plus the contract and the new cash. */
public record PurchaseResponse(
    @JsonUnwrapped TransactionResponse transaction, ContractSummary contract, BigDecimal clubCash) {

  public record ContractSummary(
      BigDecimal purchaseValue, String loyaltyUntil, ContractStatus status) {

    static ContractSummary from(Contract contract) {
      return new ContractSummary(
          contract.getPurchaseValue(),
          BrasiliaDateTime.format(contract.getLoyaltyUntil()),
          contract.getStatus());
    }
  }

  public static PurchaseResponse from(Purchase purchase) {
    return new PurchaseResponse(
        TransactionResponse.from(purchase.transaction()),
        ContractSummary.from(purchase.contract()),
        purchase.clubCash());
  }
}
