package com.dev1_isitics.ligahub_be.modules.league.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** One starter of a club lineup; a player is in at most one lineup (unique jogador_id). */
@Entity
@Table(name = "escalacoes")
@IdClass(LineupEntry.Key.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LineupEntry {

  @Id
  @Column(name = "clube_id", nullable = false)
  private Integer clubId;

  @Id
  @Column(name = "jogador_id", nullable = false)
  private Integer playerId;

  @Column(name = "criado_em", nullable = false)
  private Instant createdAt;

  public LineupEntry(Integer clubId, Integer playerId, Instant createdAt) {
    this.clubId = clubId;
    this.playerId = playerId;
    this.createdAt = createdAt;
  }

  @EqualsAndHashCode
  @NoArgsConstructor
  public static class Key implements Serializable {
    private Integer clubId;
    private Integer playerId;
  }
}
