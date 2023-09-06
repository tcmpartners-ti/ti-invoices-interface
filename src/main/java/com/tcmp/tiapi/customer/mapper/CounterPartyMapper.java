package com.tcmp.tiapi.customer.mapper;

import com.tcmp.tiapi.customer.dto.CounterPartyDTO;
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
  @Mapping(source = "customerMnemonic", target = "mnemonic")
  @Mapping(source = "name", target = "name")
  @Mapping(source = "nameAndAddressLine1", target = "address")
  @Mapping(source = "branchCode", target = "branch")
  @Mapping(source = "status", target = "status")
  @Mapping(target = "invoiceLimit.amount", expression = "java(MonetaryAmountUtils.convertCentsToDollars(counterParty.getLimitAmt()))")
  @Mapping(source = "limitCcy", target = "invoiceLimit.currency")
  CounterPartyDTO mapEntityToDTO(CounterParty counterParty);

  List<CounterPartyDTO> mapEntitiesToDTOs(List<CounterParty> counterParties);

  @Mapping(target = "id", source = "id")
  @Mapping(target = "mnemonic", expression = "java(StringMappingUtils.trimNullable(counterParty.getMnemonic()))")
  @Mapping(target = "name", expression = "java(StringMappingUtils.trimNullable(counterParty.getName()))")
  InvoiceCounterPartyDTO mapEntityToInvoiceDTO(CounterParty counterParty);
}
