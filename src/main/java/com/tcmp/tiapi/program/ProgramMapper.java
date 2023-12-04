package com.tcmp.tiapi.program;

import com.tcmp.tiapi.invoice.dto.response.InvoiceProgramDTO;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.shared.mapper.CurrencyAmountMapper;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import com.tcmp.tiapi.shared.utils.StringMappingUtils;
import java.util.List;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
    imports = {MonetaryAmountUtils.class, StringMappingUtils.class},
    uses = {CurrencyAmountMapper.class},
    componentModel = MappingConstants.ComponentModel.SPRING,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class ProgramMapper {
  @Autowired protected CurrencyAmountMapper currencyAmountMapper;

  @Mapping(target = "id", expression = "java(StringMappingUtils.trimNullable(program.getId()))")
  @Mapping(
      target = "description",
      expression = "java(StringMappingUtils.trimNullable(program.getDescription()))")
  @Mapping(
      target = "customer.mnemonic",
      expression = "java(StringMappingUtils.trimNullable(program.getCustomerMnemonic()))")
  @Mapping(source = "startDate", target = "startDate")
  @Mapping(source = "expiryDate", target = "expiryDate")
  @Mapping(source = "type", target = "type")
  @Mapping(
      target = "creditLimit",
      expression =
          "java(currencyAmountMapper.mapToDto(program.getAvailableLimitAmount(), program.getAvailableLimitCurrencyCode()))")
  @Mapping(source = "status", target = "status")
  public abstract ProgramDTO mapEntityToDTO(Program program);

  public abstract List<ProgramDTO> mapEntitiesToDTOs(List<Program> programs);

  @Mapping(target = "id", expression = "java(StringMappingUtils.trimNullable(program.getId()))")
  @Mapping(
      target = "description",
      expression = "java(StringMappingUtils.trimNullable(program.getDescription()))")
  public abstract InvoiceProgramDTO mapEntityToInvoiceDTO(Program program);
}
