package com.tcmp.tiapi.invoice.strategies.fti;

import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import com.tcmp.tiapi.invoice.service.InvoiceEventService;
import com.tcmp.tiapi.ti.model.response.ResponseStatus;
import com.tcmp.tiapi.ti.model.response.ServiceResponse;
import com.tcmp.tiapi.ti.route.FTIReplyIncomingStrategy;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingMapper;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingService;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.OperationalGatewayRequestPayload;
import com.tcmp.tiapi.titoapigee.businessbanking.model.OperationalGatewayProcessCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceCreationNotifierStrategy implements FTIReplyIncomingStrategy {
  private final InvoiceEventService invoiceEventService;
  private final BusinessBankingService businessBankingService;
  private final BusinessBankingMapper businessBankingMapper;

  @Override
  public void handleServiceResponse(ServiceResponse serviceResponse) {
    String invoiceUuidFromCorrelationId = serviceResponse.getResponseHeader().getCorrelationId();

    String responseStatus = serviceResponse.getResponseHeader().getStatus();
    boolean creationWasSuccessful = ResponseStatus.SUCCESS.getValue().equals(responseStatus);
    if (creationWasSuccessful) {
      log.info("Invoice created successfully, don't notify.");
      invoiceEventService.deleteInvoiceByUuid(invoiceUuidFromCorrelationId);
      return;
    }

    try {
      InvoiceEventInfo invoice =
          invoiceEventService.findInvoiceEventInfoByUuid(invoiceUuidFromCorrelationId);

      OperationalGatewayRequestPayload payload =
          businessBankingMapper.mapToRequestPayload(serviceResponse, invoice);

      businessBankingService.notifyEvent(OperationalGatewayProcessCode.INVOICE_CREATED, payload);
      invoiceEventService.deleteInvoiceByUuid(invoiceUuidFromCorrelationId);
    } catch (EntityNotFoundException e) {
      log.error(e.getMessage());
    }
  }
}
