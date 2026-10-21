package com.dev1_isitics.ligahub_be.modules.market.domain;

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

/** Loyalty contract created by a purchase (RN12); remaining days and penalty rate are derived. */
@Entity
@Table(name = "contratos")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Contract {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "jogador_id", nullable = false)
  private Integer playerId;

  @Column(name = "clube_id", nullable = false)
  private Integer clubId;

  @Column(name = "valor_compra", nullable = false, precision = 6, scale = 2)
  private BigDecimal purchaseValue;

  @Column(name = "fidelidade_ate", nullable = false)
  private Instant loyaltyUntil;

  @Enumerated(EnumType.STRING)
  @Column(name = "situacao", nullable = false, length = 12)
  private ContractStatus status;

  @Column(name = "iniciado_em", nullable = false)
  private Instant startedAt;

  @Column(name = "encerrado_em")
  private Instant endedAt;

  public static Contract sign(
      Integer playerId,
      Integer clubId,
      BigDecimal purchaseValue,
      Instant loyaltyUntil,
      Instant startedAt) {
    Contract contract = new Contract();
    contract.playerId = playerId;
    contract.clubId = clubId;
    contract.purchaseValue = purchaseValue;
    contract.loyaltyUntil = loyaltyUntil;
    contract.status = ContractStatus.ATIVO;
    contract.startedAt = startedAt;
    return contract;
  }

  /** On sale: RESCINDIDO when a penalty is due, ENCERRADO when the loyalty was fulfilled (RN15). */
  public void end(boolean withPenalty, Instant endedAt) {
    this.status = withPenalty ? ContractStatus.RESCINDIDO : ContractStatus.ENCERRADO;
    this.endedAt = endedAt;
  }
}
