package com.dev1_isitics.ligahub_be.modules.market.api;

import com.dev1_isitics.ligahub_be.modules.market.application.PurchaseService;
import com.dev1_isitics.ligahub_be.modules.market.application.SaleService;
import com.dev1_isitics.ligahub_be.shared.web.PathIds;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/players/" + PathIds.ID)
@RequiredArgsConstructor
public class MarketController {

  private final PurchaseService purchaseService;
  private final SaleService saleService;

  @PostMapping("/buy")
  public PurchaseResponse buy(
      @PathVariable Integer id, @Valid @RequestBody PurchaseRequest request) {
    return PurchaseResponse.from(
        purchaseService.buy(id, request.clubId(), request.loyaltyUntilInstant()));
  }

  /** Sold by the current club of the player; a body, if sent, is ignored (V1). */
  @PostMapping("/sell")
  public SaleResponse sell(@PathVariable Integer id) {
    return SaleResponse.from(saleService.sell(id));
  }
}
