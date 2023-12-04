package com.tcmp.tiapi.titoapigee.corporateloan;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.shared.exception.SimpleFeignException;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.DistributorCreditRequest;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.Data;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.DistributorCreditResponse;
import com.tcmp.tiapi.titoapigee.corporateloan.exception.CreditCreationException;
import com.tcmp.tiapi.titoapigee.security.HeaderSigner;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CorporateLoanServiceTest {
  @Mock private ObjectMapper objectMapper;
  @Mock private CorporateLoanClient corporateLoanClient;
  @Mock private HeaderSigner encryptedBodyRequestHeaderSigner;

  @Captor ArgumentCaptor<Map<String, String>> headersArgumentCaptor;

  private CorporateLoanService corporateLoanService;

  @BeforeEach
  void setUp() {
    corporateLoanService = new CorporateLoanService(
      objectMapper,
      corporateLoanClient,
      encryptedBodyRequestHeaderSigner
    );
  }

  @Test
  void createCredit_itShouldThrowExceptionOnError() {
    when(encryptedBodyRequestHeaderSigner.buildRequestHeaders(any()))
      .thenReturn(new HashMap<>());
    when(corporateLoanClient.createCredit(anyMap(), any()))
      .thenThrow(new SimpleFeignException(400, "Bad Request"));

    DistributorCreditRequest request = DistributorCreditRequest.builder().build();

    assertThrows(
      CreditCreationException.class,
      () -> corporateLoanService.createCredit(request)
    );
  }

  @Test
  void createCredit_itShouldReturnCredit() {
    when(encryptedBodyRequestHeaderSigner.buildRequestHeaders(any()))
      .thenReturn(new HashMap<>());
    when(corporateLoanClient.createCredit(anyMap(), any()))
      .thenReturn(new DistributorCreditResponse(Data.builder()
        .disbursementAmount(100)
        .build()));

    DistributorCreditResponse response = corporateLoanService.createCredit(DistributorCreditRequest.builder().build());

    verify(encryptedBodyRequestHeaderSigner).buildRequestHeaders(any());
    verify(corporateLoanClient).createCredit(headersArgumentCaptor.capture(), any());

    var actualHeaders = headersArgumentCaptor.getValue();
    assertNotNull(response);
    assertNotNull(actualHeaders.get("X-Operation-Token"));
    assertNotNull(actualHeaders.get("X-Operation-Id"));
  }
}
