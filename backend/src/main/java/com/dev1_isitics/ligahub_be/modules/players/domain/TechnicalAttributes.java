package com.dev1_isitics.ligahub_be.modules.players.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** The six attributes informed at registration; they never change afterwards (RN06). */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class TechnicalAttributes {

  @Column(name = "velocidade", nullable = false)
  private int pace;

  @Column(name = "finalizacao", nullable = false)
  private int shooting;

  @Column(name = "passe", nullable = false)
  private int passing;

  @Column(name = "drible", nullable = false)
  private int dribbling;

  @Column(name = "defesa", nullable = false)
  private int defending;

  @Column(name = "fisico", nullable = false)
  private int physical;
}
