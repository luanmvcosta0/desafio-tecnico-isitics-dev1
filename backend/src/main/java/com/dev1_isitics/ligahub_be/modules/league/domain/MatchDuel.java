package com.dev1_isitics.ligahub_be.modules.league.domain;

import com.dev1_isitics.ligahub_be.modules.players.domain.Condition;
import com.dev1_isitics.ligahub_be.modules.players.domain.Position;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** One duel as played: names, positions, conditions, attributes and values of the moment. */
@Entity
@Table(name = "duelos_partida")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchDuel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "partida_id", nullable = false)
  private Integer matchId;

  @Column(name = "ordem", nullable = false)
  private int order;

  @Column(name = "clube_atacante_id", nullable = false)
  private Integer attackingClubId;

  @Column(name = "atacante_id", nullable = false)
  private Integer attackerId;

  @Column(name = "defensor_id", nullable = false)
  private Integer defenderId;

  @Column(name = "nome_atacante", nullable = false, length = 100)
  private String attackerName;

  @Column(name = "nome_defensor", nullable = false, length = 100)
  private String defenderName;

  @Enumerated(EnumType.STRING)
  @Column(name = "posicao_atacante", nullable = false, length = 3)
  private Position attackerPosition;

  @Enumerated(EnumType.STRING)
  @Column(name = "posicao_defensor", nullable = false, length = 3)
  private Position defenderPosition;

  @Enumerated(EnumType.STRING)
  @Column(name = "condicao_atacante", nullable = false, length = 20)
  private Condition attackerCondition;

  @Enumerated(EnumType.STRING)
  @Column(name = "condicao_defensor", nullable = false, length = 20)
  private Condition defenderCondition;

  @Column(name = "atributo_ataque", nullable = false, length = 10)
  private String attackAttribute;

  @Column(name = "atributo_defesa", nullable = false, length = 10)
  private String defenseAttribute;

  @Column(name = "valor_ataque", nullable = false)
  private int attackValue;

  @Column(name = "valor_defesa", nullable = false)
  private int defenseValue;

  @Column(name = "gol", nullable = false)
  private boolean goal;

  public static MatchDuel record(Integer matchId, DuelResult duel) {
    MatchDuel recorded = new MatchDuel();
    recorded.matchId = matchId;
    recorded.order = duel.order();
    recorded.attackingClubId = duel.attackingClubId();
    recorded.attackerId = duel.attacker().getId();
    recorded.defenderId = duel.defender().getId();
    recorded.attackerName = duel.attacker().getName();
    recorded.defenderName = duel.defender().getName();
    recorded.attackerPosition = duel.attacker().getPosition();
    recorded.defenderPosition = duel.defender().getPosition();
    recorded.attackerCondition = duel.attacker().getCondition();
    recorded.defenderCondition = duel.defender().getCondition();
    recorded.attackAttribute = duel.attackAttribute().code();
    recorded.defenseAttribute = duel.defenseAttribute().code();
    recorded.attackValue = duel.attackValue();
    recorded.defenseValue = duel.defenseValue();
    recorded.goal = duel.isGoal();
    return recorded;
  }
}
