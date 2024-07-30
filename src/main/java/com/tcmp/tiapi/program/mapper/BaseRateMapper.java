package com.tcmp.tiapi.program.mapper;

import com.tcmp.tiapi.program.dto.csv.BaseRateCsvRow;
import com.tcmp.tiapi.program.dto.ti.InterestSchedule;
import com.tcmp.tiapi.shared.utils.MapperUtils;
import com.tcmp.tiapi.ti.TIServiceRequestWrapper;
import com.tcmp.tiapi.ti.dto.MaintenanceType;
import com.tcmp.tiapi.ti.dto.TIOperation;
import com.tcmp.tiapi.ti.dto.TIService;
import com.tcmp.tiapi.ti.dto.request.*;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    builder = @Builder(disableBuilder = true),
    uses = MapperUtils.class)
public abstract class BaseRateMapper {

  @Autowired private TIServiceRequestWrapper wrapper;

  @Mapping(target = "maintenanceType", expression = "java(maintenanceType)")
  @Mapping(target = "interestType", constant = "BankBase")
  @Mapping(target = "currency", constant = "USD")
  @Mapping(target = "narrative", constant = "Carga masiva de tasas")
  @Mapping(target = "scfProgramme.programmeID", source = "row.programmeId")
  @Mapping(target = "scfProgramme.seller", source = "row.seller")
  @Mapping(target = "scfProgramme.buyer", source = "row.buyer")
  @Mapping(target = "interpolate", constant = "false")
  @Mapping(target = "split", constant = "false")
  @Mapping(target = "interestDaysBasis", constant = "1")
  @Mapping(target = "periodOrAmount", constant = "A")
  @Mapping(target = "interestRateType", constant = "I")
  @Mapping(target = "tier1.rate", source = "row.rate")
  public abstract InterestSchedule mapRowToInterestSchedule(
      BaseRateCsvRow row, MaintenanceType maintenanceType);

  public ServiceRequest<InterestSchedule> mapRowToItemRequest(
      BaseRateCsvRow row, MaintenanceType maintenanceType) {
    return wrapper.wrapRequest(
        TIService.TRADE_INNOVATION,
        TIOperation.UPDATE_BASE_RATE,
        ReplyFormat.STATUS,
        null,
        mapRowToInterestSchedule(row, maintenanceType));
  }
}
