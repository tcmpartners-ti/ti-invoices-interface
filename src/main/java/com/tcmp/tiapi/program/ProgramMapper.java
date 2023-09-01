package com.tcmp.tiapi.program;

import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(imports = {MonetaryAmountUtils.class})
public interface ProgramMapper {
  @Mapping(source = "id", target = "id")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "customerMnemonic", target = "customer.mnemonic")
  @Mapping(source = "startDate", target = "startDate")
  @Mapping(source = "expiryDate", target = "endDate")
  @Mapping(source = "type", target = "type")
  @Mapping(source = "availableLimitCurrencyCode", target = "creditLimit.currency")
  @Mapping(target = "creditLimit.amount", expression = "java(MonetaryAmountUtils.convertCentsToDollars(program.getAvailableLimitAmount()))")
  @Mapping(source = "status", target = "status")
  ProgramDTO mapEntityToDTO(Program program);

  List<ProgramDTO> mapEntitiesToDTOs(List<Program> programs);
}
