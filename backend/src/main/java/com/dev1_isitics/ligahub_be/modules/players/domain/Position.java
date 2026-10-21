package com.dev1_isitics.ligahub_be.modules.players.domain;

/** Player positions, declared from back to front: this is also the squad ordering (RN07, RN11). */
public enum Position {
  GOL(new AttributeWeights(10, 0, 20, 0, 40, 30)),
  ZAG(new AttributeWeights(10, 0, 15, 0, 45, 30)),
  LAT(new AttributeWeights(25, 0, 20, 15, 25, 15)),
  MEI(new AttributeWeights(10, 15, 35, 25, 5, 10)),
  ATA(new AttributeWeights(20, 40, 10, 20, 0, 10));

  private final AttributeWeights weights;

  Position(AttributeWeights weights) {
    this.weights = weights;
  }

  public AttributeWeights weights() {
    return weights;
  }
}
