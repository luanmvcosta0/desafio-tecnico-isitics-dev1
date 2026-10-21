package com.dev1_isitics.ligahub_be.modules.market.api;

import com.dev1_isitics.ligahub_be.modules.market.application.TransactionFilter;
import com.dev1_isitics.ligahub_be.modules.market.application.TransactionQueryService;
import com.dev1_isitics.ligahub_be.shared.web.PathIds;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clubs/" + PathIds.ID + "/transactions")
@RequiredArgsConstructor
public class TransactionController {

  private final TransactionQueryService transactionQueryService;

  @GetMapping
  public TransactionHistoryResponse list(@PathVariable Integer id, TransactionFilter filter) {
    return TransactionHistoryResponse.from(transactionQueryService.history(id, filter));
  }
}
