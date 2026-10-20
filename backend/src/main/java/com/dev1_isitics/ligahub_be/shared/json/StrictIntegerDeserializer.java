package com.dev1_isitics.ligahub_be.shared.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import java.io.IOException;
import java.math.BigDecimal;

/**
 * Accepts only JSON numbers without a fractional part (V2): {@code 50} and {@code 50.0} become 50,
 * while {@code 50.5}, {@code "50"} and booleans are rejected.
 */
public class StrictIntegerDeserializer extends StdScalarDeserializer<Integer> {

  public StrictIntegerDeserializer() {
    super(Integer.class);
  }

  @Override
  public Integer deserialize(JsonParser parser, DeserializationContext context)
      throws IOException {
    JsonToken token = parser.currentToken();
    if (token == JsonToken.VALUE_NUMBER_INT || token == JsonToken.VALUE_NUMBER_FLOAT) {
      return toExactInteger(parser.getDecimalValue(), context);
    }
    return (Integer) context.handleUnexpectedToken(Integer.class, parser);
  }

  private Integer toExactInteger(BigDecimal value, DeserializationContext context)
      throws IOException {
    try {
      return value.intValueExact();
    } catch (ArithmeticException exception) {
      return (Integer)
          context.handleWeirdNumberValue(
              Integer.class, value, "deve ser um número inteiro dentro dos limites");
    }
  }
}
