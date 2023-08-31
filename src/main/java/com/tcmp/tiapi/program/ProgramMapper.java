package com.tcmp.tiapi.program;

import com.tcmp.tiapi.program.dto.request.ProgramCreationDTO;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.messaging.SCFProgrammeMessage;
import com.tcmp.tiapi.program.model.Program;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Mapper
public interface ProgramMapper {
  @Mapping(source = "id", target = "id")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "customerMnemonic", target = "customer.mnemonic")
  @Mapping(source = "startDate", target = "startDate")
  @Mapping(source = "expiryDate", target = "endDate")
  @Mapping(source = "type", target = "type")
  @Mapping(source = "availableLimitCurrencyCode", target = "creditLimit.currency")
  @Mapping(target = "creditLimit.amount", expression = "java(mapNullableBigDecimal(program.getAvailableLimitAmount()))")
  @Mapping(source = "status", target = "status")
  ProgramDTO mapEntityToDTO(Program program);

  @Mapping(source = "id", target = "id")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "customer.mnemonic", target = "customer.mnemonic")
  @Mapping(source = "startDate", target = "startDate")
  @Mapping(source = "endDate", target = "endDate")
  @Mapping(source = "type", target = "type")
  @Mapping(source = "creditLimit.amount", target = "creditLimit.amount", numberFormat = "#,###.00")
  @Mapping(source = "creditLimit.currency", target = "creditLimit.currency")
  @Mapping(source = "status", target = "status")
  SCFProgrammeMessage mapDTOToFTIMessage(ProgramCreationDTO programCreationDTO);

  default BigDecimal mapNullableBigDecimal(BigDecimal input) {
    if (input == null) return null;

    BigDecimal scaledInput = input.setScale(2, RoundingMode.HALF_UP);
    return scaledInput.divide(new BigDecimal(100), RoundingMode.HALF_UP);
  }
}
