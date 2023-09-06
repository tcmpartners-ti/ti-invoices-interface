package com.tcmp.tiapi.program;

import com.tcmp.tiapi.invoice.dto.response.InvoiceProgramDTO;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import com.tcmp.tiapi.shared.utils.StringMappingUtils;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.util.List;

@Mapper(
  imports = {
    MonetaryAmountUtils.class,
    StringMappingUtils.class
  },
  componentModel = MappingConstants.ComponentModel.SPRING,
  injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface ProgramMapper {
  @Mapping(source = "id", target = "id")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "customerMnemonic", target = "customer.mnemonic")
  @Mapping(source = "startDate", target = "startDate")
  @Mapping(source = "expiryDate", target = "endDate")
  @Mapping(source = "type", target = "type")
  @Mapping(target = "creditLimit.amount", expression = "java(MonetaryAmountUtils.convertCentsToDollars(program.getAvailableLimitAmount()))")
  @Mapping(source = "availableLimitCurrencyCode", target = "creditLimit.currency")
  @Mapping(source = "status", target = "status")
  ProgramDTO mapEntityToDTO(Program program);

  List<ProgramDTO> mapEntitiesToDTOs(List<Program> programs);

  @Mapping(target = "id", expression = "java(StringMappingUtils.trimNullable(program.getId()))")
  @Mapping(target = "description", expression = "java(StringMappingUtils.trimNullable(program.getDescription()))")
  InvoiceProgramDTO mapEntityToInvoiceDTO(Program program);
}
