package com.tcmp.tiapi.invoice.strategy.ftireply;

import com.tcmp.tiapi.ti.dto.response.ServiceResponse;
import com.tcmp.tiapi.ti.route.fti.FTIReplyIncomingStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceCreationStatusNotifierStrategy implements FTIReplyIncomingStrategy {
  private final InvoiceCreationStatusSftpNotifier statusSftpNotifier;
  private final InvoiceCreationStatusBusinessBankingNotifier businessBankingNotifier;

  @Override
  public void handleServiceResponse(ServiceResponse serviceResponse) {
    String invoiceUuidFromCorrelationId = serviceResponse.getResponseHeader().getCorrelationId();

    boolean isSftpChannelCorrelationUuid = invoiceUuidFromCorrelationId.split(":").length == 2;
    if (isSftpChannelCorrelationUuid) {
      statusSftpNotifier.notify(serviceResponse);
      return;
    }

    businessBankingNotifier.notify(serviceResponse);
  }
}
