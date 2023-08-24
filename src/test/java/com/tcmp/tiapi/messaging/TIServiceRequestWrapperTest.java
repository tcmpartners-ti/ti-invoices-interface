package com.tcmp.tiapi.messaging;

import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.messaging.model.TIService;
import com.tcmp.tiapi.messaging.model.requests.ReplyFormat;
import com.tcmp.tiapi.messaging.model.requests.ServiceRequest;
import com.tcmp.tiapi.program.messaging.SCFProgrammeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TIServiceRequestWrapperTest {
  TIServiceRequestWrapper testedServiceRequestWrapper;

  @BeforeEach
  void beforeAll() {
    testedServiceRequestWrapper = new TIServiceRequestWrapper();
  }

  @Test
  void itShouldWrapMessage() {
    // Given
    String expectedProgrammeId = "PROG123";

    SCFProgrammeMessage programmeMessage = SCFProgrammeMessage.builder()
      .id(expectedProgrammeId)
      .build();
    // When
    ServiceRequest<SCFProgrammeMessage> serviceRequest =
      testedServiceRequestWrapper.wrapRequest(
        TIService.TRADE_INNOVATION,
        TIOperation.SCF_PROGRAMME,
        ReplyFormat.STATUS,
        programmeMessage
      );

    // Then
    String actualProgrammeId = serviceRequest.getBody().getId();
    assertEquals(expectedProgrammeId, actualProgrammeId);
  }
}
