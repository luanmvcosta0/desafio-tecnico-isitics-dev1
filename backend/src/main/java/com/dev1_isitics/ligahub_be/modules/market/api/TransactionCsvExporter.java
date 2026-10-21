package com.dev1_isitics.ligahub_be.modules.market.api;

import com.dev1_isitics.ligahub_be.modules.market.domain.Transaction;
import com.dev1_isitics.ligahub_be.shared.time.BrasiliaDateTime;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Pattern;

/**
 * CSV of the history (RN18): {@code ;} separator, values with 2 decimals and decimal comma, empty
 * profit when null, player name quoted when it has {@code ;} or quotes, UTF-8 without BOM and every
 * line ending with LF, including the last one.
 */
public final class TransactionCsvExporter {

  private static final String HEADER =
      "data_hora;tipo;jogador;valor;taxa_ou_comissao;multa;lucro;variacao_caixa";
  private static final String SEPARATOR = ";";
  private static final String LINE_END = "\n";
  private static final String QUOTE = "\"";
  private static final Pattern NEEDS_QUOTES = Pattern.compile("[;\"]");

  private TransactionCsvExporter() {}

  public static byte[] export(List<Transaction> transactions) {
    StringBuilder csv = new StringBuilder(HEADER).append(LINE_END);
    transactions.forEach(transaction -> csv.append(line(transaction)).append(LINE_END));
    return csv.toString().getBytes(StandardCharsets.UTF_8);
  }

  private static String line(Transaction transaction) {
    return String.join(
        SEPARATOR,
        BrasiliaDateTime.format(transaction.getCreatedAt()),
        transaction.getType().name(),
        quoteIfNeeded(transaction.getPlayerName()),
        amount(transaction.getValue()),
        amount(transaction.getFee()),
        amount(transaction.getPenalty()),
        transaction.getProfit() == null ? "" : amount(transaction.getProfit()),
        amount(transaction.getCashChange()));
  }

  private static String amount(BigDecimal value) {
    return value.setScale(2).toPlainString().replace('.', ',');
  }

  private static String quoteIfNeeded(String text) {
    if (!NEEDS_QUOTES.matcher(text).find()) {
      return text;
    }
    return QUOTE + text.replace(QUOTE, QUOTE + QUOTE) + QUOTE;
  }
}
