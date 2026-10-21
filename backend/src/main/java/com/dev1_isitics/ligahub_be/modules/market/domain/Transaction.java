package com.dev1_isitics.ligahub_be.modules.market.domain;

import com.dev1_isitics.ligahub_be.shared.money.Money;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Purchase or sale as it happened (RN17): values and player name of the moment, never recalculated
 * nor edited afterwards.
 */
@Entity
@Table(name = "transacoes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "clube_id", nullable = false)
  private Integer clubId;

  @Column(name = "jogador_id", nullable = false)
  private Integer playerId;

  @Column(name = "nome_jogador", nullable = false, length = 100)
  private String playerName;

  @Enumerated(EnumType.STRING)
  @Column(name = "tipo", nullable = false, length = 6)
  private TransactionType type;

  @Column(name = "valor", nullable = false, precision = 6, scale = 2)
  private BigDecimal value;

  @Column(name = "taxa", nullable = false, precision = 6, scale = 2)
  private BigDecimal fee;

  @Column(name = "multa", nullable = false, precision = 6, scale = 2)
  private BigDecimal penalty;

  @Column(name = "lucro", precision = 7, scale = 2)
  private BigDecimal profit;

  @Column(name = "variacao_caixa", nullable = false, precision = 8, scale = 2)
  private BigDecimal cashChange;

  @Column(name = "criado_em", nullable = false)
  private Instant createdAt;

  /** Purchase: no penalty nor profit, and the cash goes down by value plus fee. */
  public static Transaction purchase(
      Integer clubId,
      Integer playerId,
      String playerName,
      BigDecimal value,
      BigDecimal fee,
      Instant createdAt) {
    Transaction transaction = new Transaction();
    transaction.clubId = clubId;
    transaction.playerId = playerId;
    transaction.playerName = playerName;
    transaction.type = TransactionType.COMPRA;
    transaction.value = value;
    transaction.fee = fee;
    transaction.penalty = Money.ZERO;
    transaction.cashChange = value.add(fee).negate();
    transaction.createdAt = createdAt;
    return transaction;
  }

  /** Sale: the fee is the commission and the cash goes up by value minus penalty and fee. */
  public static Transaction sale(
      Integer clubId,
      Integer playerId,
      String playerName,
      BigDecimal value,
      BigDecimal commission,
      BigDecimal penalty,
      BigDecimal profit,
      Instant createdAt) {
    Transaction transaction = new Transaction();
    transaction.clubId = clubId;
    transaction.playerId = playerId;
    transaction.playerName = playerName;
    transaction.type = TransactionType.VENDA;
    transaction.value = value;
    transaction.fee = commission;
    transaction.penalty = penalty;
    transaction.profit = profit;
    transaction.cashChange = value.subtract(penalty).subtract(commission);
    transaction.createdAt = createdAt;
    return transaction;
  }
}
