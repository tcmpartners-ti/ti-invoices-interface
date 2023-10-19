package com.tcmp.tiapi.customer.mapper;

import com.tcmp.tiapi.customer.dto.request.CounterPartyDTO;
import com.tcmp.tiapi.customer.model.CounterParty;
import com.tcmp.tiapi.invoice.dto.response.InvoiceCounterPartyDTO;
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
public interface CounterPartyMapper {
  @Mapping(target = "mnemonic", expression = "java(StringMappingUtils.trimNullable(counterParty.getMnemonic()))")
  @Mapping(target = "name", expression = "java(StringMappingUtils.trimNullable(counterParty.getName()))")
  @Mapping(target = "address", expression = "java(StringMappingUtils.trimNullable(counterParty.getNameAndAddressLine1()))")
  @Mapping(target = "branch", expression = "java(StringMappingUtils.trimNullable(counterParty.getBranchCode()))")
  @Mapping(source = "status", target = "status")
  @Mapping(target = "invoiceLimit.amount", expression = "java(MonetaryAmountUtils.convertCentsToDollars(counterParty.getLimitAmt()))")
  @Mapping(target = "invoiceLimit.currency", expression = "java(StringMappingUtils.trimNullable(counterParty.getLimitCcy()))")
  CounterPartyDTO mapEntityToDTO(CounterParty counterParty);

  List<CounterPartyDTO> mapEntitiesToDTOs(List<CounterParty> counterParties);

  @Mapping(target = "id", source = "id")
  @Mapping(target = "mnemonic", expression = "java(StringMappingUtils.trimNullable(counterParty.getMnemonic()))")
  @Mapping(target = "name", expression = "java(StringMappingUtils.trimNullable(counterParty.getName()))")
  InvoiceCounterPartyDTO mapEntityToInvoiceDTO(CounterParty counterParty);
}
