package com.dev1_isitics.ligahub_be.modules.players.domain;

import java.math.BigDecimal;

/** Physical condition and its market value discount, in percentage points (RN02). */
public enum Condition {
  APTO(BigDecimal.ZERO),
  MAL_CONDICIONADO(BigDecimal.valueOf(15)),
  DOENTE(BigDecimal.valueOf(30));

  private final BigDecimal discountPercent;

  Condition(BigDecimal discountPercent) {
    this.discountPercent = discountPercent;
  }

  public BigDecimal discountPercent() {
    return discountPercent;
  }
}
