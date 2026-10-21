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
 * this entity: {@link #overall()} and {@link #marketValue()} delegate to their calculators. The
 * club is referenced by id to keep the players aggregate independent of clubs.
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

  /**
   * Registers a free and fit player (RN06): the base value is computed once, from the overall and
   * age at registration, and never changes afterwards.
   */
  public static Player register(
      String name,
      Position position,
      int agility,
      int age,
      TechnicalAttributes attributes,
      int potential) {
    Player player = new Player();
    player.name = name;
    player.position = position;
    player.agility = agility;
    player.age = age;
    player.attributes = attributes;
    player.potential = potential;
    player.condition = Condition.APTO;
    player.baseValue = BaseValueCalculator.calculate(player.overall(), potential, age);
    return player;
  }

  /**
   * Edition (RN08): only these four fields change; attributes and base value stay as registered.
   */
  public void edit(String name, Position position, int agility, int age) {
    this.name = name;
    this.position = position;
    this.agility = agility;
    this.age = age;
  }

  public void changeCondition(Condition condition) {
    this.condition = condition;
  }

  /** Logical deletion (RN09): nothing is removed, the player just stops existing for the API. */
  public void delete(Instant deletedAt) {
    this.deletedAt = deletedAt;
  }

  /** The club changes only by purchases and sales (RN12, RN16). */
  public void joinClub(Integer clubId) {
    this.clubId = clubId;
  }

  public void leaveClub() {
    this.clubId = null;
  }

  public boolean isFree() {
    return clubId == null;
  }

  public int overall() {
    return OverallCalculator.calculate(position, attributes);
  }

  public BigDecimal marketValue() {
    return MarketValueCalculator.calculate(baseValue, condition, agility, age);
  }
}
