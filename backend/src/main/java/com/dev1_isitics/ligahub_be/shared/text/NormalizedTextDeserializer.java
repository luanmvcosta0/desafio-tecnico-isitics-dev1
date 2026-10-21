package com.dev1_isitics.ligahub_be.shared.text;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import java.io.IOException;

/**
 * Reads a JSON string already normalized by {@link TextNormalizer} (V4), so size limits are checked
 * on the normalized value. Text with control characters is rejected and blank text becomes absent.
 */
public class NormalizedTextDeserializer extends StdScalarDeserializer<String> {

  public NormalizedTextDeserializer() {
    super(String.class);
  }

  @Override
  public String deserialize(JsonParser parser, DeserializationContext context) throws IOException {
    if (parser.currentToken() != JsonToken.VALUE_STRING) {
      return (String) context.handleUnexpectedToken(String.class, parser);
    }
    String text = parser.getText();
    if (TextNormalizer.containsControlCharacter(text)) {
      return (String)
          context.handleWeirdStringValue(String.class, text, "não pode ter caracteres de controle");
    }
    return TextNormalizer.normalize(text);
  }
}
