package com.tcmp.tiapi.titoapigee.mapper;

import com.tcmp.tiapi.invoice.dto.request.InvoiceCorrelationPayload;
import com.tcmp.tiapi.messaging.model.response.ServiceResponse;
import com.tcmp.tiapi.titoapigee.dto.request.OperationalGatewayRequestPayload;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(
  componentModel = MappingConstants.ComponentModel.SPRING,
  injectionStrategy = InjectionStrategy.CONSTRUCTOR
)
public interface OperationalGatewayMapper {
  @Mapping(source = "serviceResponse.responseHeader.status", target = "status")
  @Mapping(source = "serviceResponse.responseHeader.details", target = "details")
  @Mapping(source = "invoice", target = "invoice")
  OperationalGatewayRequestPayload mapTiServiceResponseToOperationalGatewayPayload(
    ServiceResponse serviceResponse,
    InvoiceCorrelationPayload invoice
  );
}
