package com.tcmp.tiapi.titoapigee.businessbanking;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tcmp.tiapi.shared.UUIDGenerator;
import com.tcmp.tiapi.titoapigee.businessbanking.model.OperationalGatewayProcessCode;
import com.tcmp.tiapi.titoapigee.security.HeaderSigner;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BusinessBankingServiceTest {
  @Mock private HeaderSigner encryptedBodyRequestHeaderSigner;
  @Mock private BusinessBankingClient businessBankingClient;
  @Mock private UUIDGenerator uuidGenerator;

  @InjectMocks private BusinessBankingService businessBankingService;

  @Test
  void notifyEvent_itShouldSendRequest() {
    var requestUuid = "000-0001";

    when(encryptedBodyRequestHeaderSigner.buildRequestHeaders(any()))
        .thenReturn(Map.of("Headers", "Yes!"));
    when(uuidGenerator.getNewId()).thenReturn(requestUuid);

    businessBankingService.notifyEvent(
        OperationalGatewayProcessCode.INVOICE_CREATED,
        Map.of("message", "This is a test payload."));

    verify(businessBankingClient).notifyEvent(anyMap(), any());
  }
}
