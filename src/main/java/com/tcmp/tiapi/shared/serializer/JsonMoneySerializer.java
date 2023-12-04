package com.tcmp.tiapi.shared.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;

public class JsonMoneySerializer extends JsonSerializer<BigDecimal> {
  private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

  @Override
  public void serialize(
      BigDecimal value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {
    if (value == null) return;
    decimalFormat.setParseBigDecimal(true);

    jsonGenerator.writeNumber(decimalFormat.format(value));
  }
}
