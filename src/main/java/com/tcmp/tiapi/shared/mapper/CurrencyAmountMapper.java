package com.tcmp.tiapi.shared.mapper;

import com.tcmp.tiapi.shared.dto.response.CurrencyAmountDTO;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import com.tcmp.tiapi.shared.utils.StringMappingUtils;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import java.math.BigDecimal;

@Mapper(
  componentModel = MappingConstants.ComponentModel.SPRING,
  injectionStrategy = InjectionStrategy.CONSTRUCTOR,
  imports = {
    BigDecimal.class,
    MonetaryAmountUtils.class,
    StringMappingUtils.class
  }
)
public interface CurrencyAmountMapper {
  @Mapping(target = "amount", expression = "java(MonetaryAmountUtils.convertCentsToDollars(amount))")
  @Mapping(target = "currency", expression = "java(StringMappingUtils.trimNullable(currencyCode))")
  CurrencyAmountDTO mapToDto(BigDecimal amount, String currencyCode);
}
