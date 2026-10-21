package com.dev1_isitics.ligahub_be.modules.market.api;

import com.dev1_isitics.ligahub_be.modules.market.application.TransactionFilter;
import com.dev1_isitics.ligahub_be.modules.market.application.TransactionQueryService;
import com.dev1_isitics.ligahub_be.shared.web.PathIds;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clubs/" + PathIds.ID + "/transactions")
@RequiredArgsConstructor
public class TransactionController {

  private static final MediaType TEXT_CSV = new MediaType("text", "csv", StandardCharsets.UTF_8);
  private static final String CSV_FILE_NAME = "historico-clube-%d.csv";

  private final TransactionQueryService transactionQueryService;

  @GetMapping
  public TransactionHistoryResponse list(@PathVariable Integer id, TransactionFilter filter) {
    return TransactionHistoryResponse.from(transactionQueryService.history(id, filter));
  }

  /** Errors (invalid filter, unknown club or player) stay in JSON, like every other endpoint. */
  @GetMapping("/export")
  public ResponseEntity<byte[]> export(@PathVariable Integer id, TransactionFilter filter) {
    byte[] csv =
        TransactionCsvExporter.export(transactionQueryService.history(id, filter).transactions());
    return ResponseEntity.ok()
        .contentType(TEXT_CSV)
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment()
                .filename(CSV_FILE_NAME.formatted(id))
                .build()
                .toString())
        .body(csv);
  }
}
