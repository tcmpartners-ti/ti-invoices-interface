package com.tcmp.tiapi.customer.mapper;

import com.tcmp.tiapi.customer.dto.request.CounterPartyDTO;
import com.tcmp.tiapi.customer.dto.response.SellerInfoDTO;
import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.CounterParty;
import com.tcmp.tiapi.customer.repository.AccountRepository;
import com.tcmp.tiapi.invoice.dto.response.InvoiceCounterPartyDTO;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import com.tcmp.tiapi.shared.utils.StringMappingUtils;

import java.util.ArrayList;
import java.util.List;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
    imports = {MonetaryAmountUtils.class, StringMappingUtils.class},
    componentModel = MappingConstants.ComponentModel.SPRING,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        builder = @Builder(disableBuilder = true)
)
public abstract class CounterPartyMapper {

  @Autowired private AccountRepository accountRepository;

  @Mapping(
      target = "mnemonic",
      expression = "java(StringMappingUtils.trimNullable(counterParty.getMnemonic()))")
  @Mapping(
      target = "name",
      expression = "java(StringMappingUtils.trimNullable(counterParty.getName()))")
  @Mapping(
      target = "address",
      expression = "java(StringMappingUtils.trimNullable(counterParty.getNameAndAddressLine1()))")
  @Mapping(
      target = "branch",
      expression = "java(StringMappingUtils.trimNullable(counterParty.getBranchCode()))")
  @Mapping(source = "status", target = "status")
  @Mapping(
      target = "invoiceLimit.amount",
      expression = "java(MonetaryAmountUtils.convertCentsToDollars(counterParty.getLimitAmt()))")
  @Mapping(
      target = "invoiceLimit.currency",
      expression = "java(StringMappingUtils.trimNullable(counterParty.getLimitCcy()))")
  public abstract CounterPartyDTO mapEntityToDTO(CounterParty counterParty);

  public abstract List<CounterPartyDTO> mapEntitiesToDTOs(List<CounterParty> counterParties);

  @Mapping(target = "id", source = "id")
  @Mapping(
      target = "mnemonic",
      expression = "java(StringMappingUtils.trimNullable(counterParty.getMnemonic()))")
  @Mapping(
      target = "name",
      expression = "java(StringMappingUtils.trimNullable(counterParty.getName()))")
  public abstract InvoiceCounterPartyDTO mapEntityToInvoiceDTO(CounterParty counterParty);


  @Mapping(
      target = "mnemonic",
      expression = "java(StringMappingUtils.trimNullable(counterParty.getMnemonic()))")
  @Mapping(
      target = "name",
      expression = "java(StringMappingUtils.trimNullable(counterParty.getName()))")
  @Mapping(
      target = "address",
      expression = "java(StringMappingUtils.trimNullable(counterParty.getNameAndAddressLine1()))")
  @Mapping(
      target = "email",
      expression = "java(StringMappingUtils.trimNullable(counterParty.getEmail()))")
  @Mapping(
      target = "phone",
      expression = "java(StringMappingUtils.trimNullable(counterParty.getPhone()))")
  @Mapping(
      target = "account.accountType",
      expression = "java(StringMappingUtils.trimNullable(account.getType()))")
  @Mapping(
      target = "account.accountNumber",
      expression = "java(StringMappingUtils.trimNullable(account.getExternalAccountNumber()))")
  @Mapping(
      target = "relation.limitAmount",
      expression = "java(MonetaryAmountUtils.convertCentsToDollars(counterParty.getScfMap().getLimitAmt()))")
  @Mapping(
      target = "relation.maxNumAdvance",
      expression = "java(counterParty.getScfMap().getMaxPerNum())")
  @Mapping(
      target = "relation.maxPercentAdvance",
      expression = "java(counterParty.getScfMap().getAdvncePct())")
  @Mapping(source = "counterParty.status", target = "status")
  public abstract SellerInfoDTO mapEntityToSellerInfoDTO(
      CounterParty counterParty, Account account);

  public List<SellerInfoDTO> mapEntitiesToSellerInfoDTOs(List<CounterParty> counterParties) {
    List<SellerInfoDTO> sellerInfoDTOs = new ArrayList<>(counterParties.size());
    for (CounterParty counterParty : counterParties) {
      if(counterParty.getScfMap() != null){
        Account account =
                accountRepository.findAccountWithMaxSequenceNumberByCustomerMnemonic(
                        counterParty.getCustomerMnemonic());
        sellerInfoDTOs.add(mapEntityToSellerInfoDTO(counterParty, account));
      }
    }

    return sellerInfoDTOs;
  }
}
