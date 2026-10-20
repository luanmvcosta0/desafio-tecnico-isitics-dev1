package com.dev1_isitics.ligahub_be.shared.config;

import com.dev1_isitics.ligahub_be.shared.json.StrictIntegerDeserializer;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.type.LogicalType;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * JSON contract of the API: snake_case names (API conventions), unknown fields ignored (V7) and no
 * implicit type conversion, so each field must arrive with its declared type (V2, V5).
 */
@Configuration
public class JacksonConfig {

  @Bean
  public Jackson2ObjectMapperBuilderCustomizer apiContractCustomizer() {
    return builder ->
        builder
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .featuresToDisable(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                MapperFeature.ALLOW_COERCION_OF_SCALARS)
            .featuresToEnable(
                DeserializationFeature.FAIL_ON_NUMBERS_FOR_ENUMS,
                DeserializationFeature.FAIL_ON_TRAILING_TOKENS,
                JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN)
            .modulesToInstall(strictIntegerModule())
            .postConfigurer(
                mapper ->
                    mapper
                        .coercionConfigFor(LogicalType.Textual)
                        .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                        .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail));
  }

  private SimpleModule strictIntegerModule() {
    StrictIntegerDeserializer deserializer = new StrictIntegerDeserializer();
    return new SimpleModule("StrictIntegerModule")
        .addDeserializer(Integer.class, deserializer)
        .addDeserializer(int.class, deserializer);
  }
}
