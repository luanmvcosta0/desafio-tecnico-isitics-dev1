package com.dev1_isitics.ligahub_be.modules.market.api;

import com.dev1_isitics.ligahub_be.modules.market.domain.Transaction;
import com.dev1_isitics.ligahub_be.modules.market.domain.TransactionType;
import com.dev1_isitics.ligahub_be.shared.time.BrasiliaDateTime;
import java.math.BigDecimal;

/** A purchase or sale as recorded (RN17), with the date-time in Brasília time. */
public record TransactionResponse(
    Integer id,
    Integer clubId,
    Integer playerId,
    String playerName,
    TransactionType type,
    BigDecimal value,
    BigDecimal fee,
    BigDecimal penalty,
    BigDecimal profit,
    BigDecimal cashChange,
    String createdAt) {

  public static TransactionResponse from(Transaction transaction) {
    return new TransactionResponse(
        transaction.getId(),
        transaction.getClubId(),
        transaction.getPlayerId(),
        transaction.getPlayerName(),
        transaction.getType(),
        transaction.getValue(),
        transaction.getFee(),
        transaction.getPenalty(),
        transaction.getProfit(),
        transaction.getCashChange(),
        BrasiliaDateTime.format(transaction.getCreatedAt()));
  }
}
