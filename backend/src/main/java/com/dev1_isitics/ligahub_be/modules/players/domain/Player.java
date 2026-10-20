package com.dev1_isitics.ligahub_be.modules.players.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

/**
 * Market value and overall are derived on every read (RN05, RN06) and therefore are not fields of
 * this entity. The club is referenced by id to keep the players aggregate independent of clubs.
 */
@Entity
@Table(name = "jogadores")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Player {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "nome", nullable = false, length = 100)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "posicao", nullable = false, length = 3)
  private Position position;

  @Column(name = "agilidade", nullable = false)
  private int agility;

  @Column(name = "idade", nullable = false)
  private int age;

  @Column(name = "valor_base", nullable = false, precision = 6, scale = 2)
  private BigDecimal baseValue;

  @Enumerated(EnumType.STRING)
  @Column(name = "condicao", nullable = false, length = 20)
  private Condition condition;

  @Column(name = "clube_id")
  private Integer clubId;

  @Embedded private TechnicalAttributes attributes;

  @Column(name = "potencial", nullable = false)
  private int potential;

  @Column(name = "excluido_em")
  private Instant deletedAt;

  @CreationTimestamp
  @Column(name = "criado_em", nullable = false, updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "atualizado_em", nullable = false)
  private Instant updatedAt;
}
