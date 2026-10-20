package com.dev1_isitics.ligahub_be.shared.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dev1_isitics.ligahub_be.modules.players.domain.Position;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

class JacksonConfigTest {

  private record SampleRequest(String playerName, Integer age, Position position) {}

  private record SampleResponse(BigDecimal marketValue) {}

  private final ObjectMapper mapper = buildMapper();

  private static ObjectMapper buildMapper() {
    Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
    new JacksonConfig().apiContractCustomizer().customize(builder);
    return builder.build();
  }

  private SampleRequest read(String json) throws JsonProcessingException {
    return mapper.readValue(json, SampleRequest.class);
  }

  @Test
  void readsSnakeCaseFields() throws JsonProcessingException {
    SampleRequest request = read("{\"player_name\": \"Meia Lua\", \"age\": 25, \"position\": \"MEI\"}");

    assertThat(request).isEqualTo(new SampleRequest("Meia Lua", 25, Position.MEI));
  }

  @Test
  void ignoresUnknownAndCamelCaseFields() throws JsonProcessingException {
    SampleRequest request = read("{\"playerName\": \"Meia Lua\", \"base_value\": 99.0}");

    assertThat(request.playerName()).isNull();
  }

  @Test
  void acceptsIntegerWrittenWithZeroFraction() throws JsonProcessingException {
    assertThat(read("{\"age\": 50.0}").age()).isEqualTo(50);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "{\"age\": 50.5}",
        "{\"age\": \"15\"}",
        "{\"age\": true}",
        "{\"age\": 2147483648}",
        "{\"player_name\": 123}",
        "{\"player_name\": true}",
        "{\"position\": \"mei\"}",
        "{\"position\": 0}",
        "[]",
        "\"x\"",
        "10",
        "{\"age\": 25} {}"
      })
  void rejectsValuesOutsideTheDeclaredType(String json) {
    assertThatThrownBy(() -> read(json)).isInstanceOf(JsonProcessingException.class);
  }

  @Test
  void writesSnakeCaseFieldsAndPlainDecimals() throws JsonProcessingException {
    String json = mapper.writeValueAsString(new SampleResponse(new BigDecimal("40.00")));

    assertThat(json).isEqualTo("{\"market_value\":40.00}");
  }
}
