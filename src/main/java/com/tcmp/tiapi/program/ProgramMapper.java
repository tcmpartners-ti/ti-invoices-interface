package com.tcmp.tiapi.program;

import com.tcmp.tiapi.invoice.dto.response.InvoiceProgramDTO;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.shared.mapper.CurrencyAmountMapper;
import com.tcmp.tiapi.shared.utils.MapperUtils;
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
    componentModel = MappingConstants.ComponentModel.SPRING,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = {CurrencyAmountMapper.class, MapperUtils.class})
public abstract class ProgramMapper {
  @Autowired protected CurrencyAmountMapper currencyAmountMapper;

  @Mapping(source = "id", target = "id")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "customer.id.mnemonic", target = "customer.mnemonic")
  @Mapping(source = "customer.type", target = "customer.commercialTradeCode")
  @Mapping(source = "startDate", target = "startDate")
  @Mapping(source = "expiryDate", target = "expiryDate")
  @Mapping(source = "type", target = "type")
  @Mapping(source = "status", target = "status")
  @Mapping(
      target = "creditLimit",
      expression =
          "java(currencyAmountMapper.mapToDto(program.getAvailableLimitAmount(), program.getAvailableLimitCurrencyCode()))")
  @Mapping(
      source = "extension.extraFinancingDays",
      target = "extraFinancingDays",
      defaultValue = "0")
  public abstract ProgramDTO mapEntityToDTO(Program program);

  public abstract List<ProgramDTO> mapEntitiesToDTOs(List<Program> programs);

  public abstract InvoiceProgramDTO mapEntityToInvoiceDTO(Program program);
}
