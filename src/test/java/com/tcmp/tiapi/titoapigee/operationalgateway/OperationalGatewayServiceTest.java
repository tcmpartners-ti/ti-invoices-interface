package com.tcmp.tiapi.titoapigee.operationalgateway;

import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import com.tcmp.tiapi.titoapigee.operationalgateway.dto.response.Channel;
import com.tcmp.tiapi.titoapigee.operationalgateway.dto.response.NotificationInfo;
import com.tcmp.tiapi.titoapigee.operationalgateway.dto.response.NotificationsResponse;
import com.tcmp.tiapi.titoapigee.operationalgateway.dto.response.Recipient;
import com.tcmp.tiapi.titoapigee.security.HeaderSigner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationalGatewayServiceTest {
  @Mock private HeaderSigner operationalGatewayHeaderSigner;
  @Mock private OperationalGatewayClient operationalGatewayClient;

  private OperationalGatewayService testedOperationalGatewayService;

  @BeforeEach
  void setUo() {
    testedOperationalGatewayService = new OperationalGatewayService(
      operationalGatewayHeaderSigner,
      operationalGatewayClient
    );
  }

  @Test
  void sendEmailNotification_itShouldSendEmail() {
    when(operationalGatewayClient.sendEmailNotification(anyMap(), any()))
      .thenReturn(new NotificationsResponse(List.of(
        new NotificationInfo(
          new Recipient(
            new Channel("david@mail.com", "email")
          )
        )
      )));

    testedOperationalGatewayService.sendEmailNotification(
      "", "david@mail.com", "", List.of());

    verify(operationalGatewayHeaderSigner).buildRequestHeaders(any(ApiGeeBaseRequest.class));
    //noinspection unchecked
    verify(operationalGatewayClient).sendEmailNotification(anyMap(), any(ApiGeeBaseRequest.class));
  }
}
