package com.tcmp.tiapi.program.converter;

import com.tcmp.tiapi.program.model.ProgramStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Objects;

@Converter
public class DatabaseProgramStatusConverter implements AttributeConverter<ProgramStatus, String> {
  @Override
  public String convertToDatabaseColumn(ProgramStatus status) {
    return Objects.requireNonNullElse(status, ProgramStatus.INACTIVE).getValue();

  }

  @Override
  public ProgramStatus convertToEntityAttribute(String dbData) {
    return ProgramStatus.from(dbData);
  }
}
