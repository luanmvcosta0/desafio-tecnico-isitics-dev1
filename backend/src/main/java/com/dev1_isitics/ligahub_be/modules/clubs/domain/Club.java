package com.dev1_isitics.ligahub_be.modules.clubs.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/** Club of the league; the cash changes only by purchases and sales (RN10). */
@Entity
@Table(name = "clubes")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Club {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Integer id;

  @Column(name = "nome", nullable = false, length = 60)
  private String name;

  @JdbcTypeCode(SqlTypes.CHAR)
  @Column(name = "sigla", nullable = false, length = 3)
  private String acronym;

  @Column(name = "caixa", nullable = false, precision = 10, scale = 2)
  private BigDecimal cash;

  @CreationTimestamp
  @Column(name = "criado_em", nullable = false, updatable = false)
  private Instant createdAt;
}
