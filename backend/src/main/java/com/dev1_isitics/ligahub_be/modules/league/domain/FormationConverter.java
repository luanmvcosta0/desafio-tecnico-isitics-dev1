package com.dev1_isitics.ligahub_be.modules.league.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** Stores the formation with the code of the statement ("2-1-1") in the database. */
@Converter
public class FormationConverter implements AttributeConverter<Formation, String> {

  @Override
  public String convertToDatabaseColumn(Formation formation) {
    return formation == null ? null : formation.code();
  }

  @Override
  public Formation convertToEntityAttribute(String code) {
    return code == null ? null : Formation.fromCode(code);
  }
}
