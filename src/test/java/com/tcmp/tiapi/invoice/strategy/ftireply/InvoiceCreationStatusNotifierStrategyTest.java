package com.tcmp.tiapi.invoice.strategy.ftireply;

import static org.mockito.Mockito.*;

import com.tcmp.tiapi.ti.dto.response.ResponseHeader;
import com.tcmp.tiapi.ti.dto.response.ServiceResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InvoiceCreationStatusNotifierStrategyTest {
  @Mock private InvoiceCreationStatusSftpNotifier invoiceCreationStatusSftpNotifier;

  @Mock
  private InvoiceCreationStatusBusinessBankingNotifier invoiceCreationStatusBusinessBankingNotifier;

  @InjectMocks private InvoiceCreationStatusNotifierStrategy invoiceCreationStatusNotifierStrategy;

  @Test
  void handleServiceResponse_itShouldSendToSftpChannel() {
    var sftpUuid = "abc:1";
    var response =
        ServiceResponse.builder()
            .responseHeader(ResponseHeader.builder().correlationId(sftpUuid).build())
            .build();

    invoiceCreationStatusNotifierStrategy.handleServiceResponse(response);

    verify(invoiceCreationStatusSftpNotifier).notify(any(ServiceResponse.class));
    verifyNoInteractions(invoiceCreationStatusBusinessBankingNotifier);
  }

  @Test
  void handleServiceResponse_itShouldSendToBusinessBankingChannel() {
    var businessBankingUuid = "abc";
    var response =
        ServiceResponse.builder()
            .responseHeader(ResponseHeader.builder().correlationId(businessBankingUuid).build())
            .build();

    invoiceCreationStatusNotifierStrategy.handleServiceResponse(response);

    verify(invoiceCreationStatusBusinessBankingNotifier).notify(any(ServiceResponse.class));
    verifyNoInteractions(invoiceCreationStatusSftpNotifier);
  }
}
