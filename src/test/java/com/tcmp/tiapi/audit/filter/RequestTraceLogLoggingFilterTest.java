package com.tcmp.tiapi.audit.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RequestTraceLogLoggingFilterTest {
  private RequestTraceLogLoggingFilter filter;
  private MockHttpServletRequest request;
  private MockHttpServletResponse response;

  @Mock private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    filter = new RequestTraceLogLoggingFilter(objectMapper);
    request = new MockHttpServletRequest();
    response = new MockHttpServletResponse();

    request.setContent("{}".getBytes());
    request.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
  }

  @Test
  void doFilterInternal() throws ServletException, IOException {
    when(objectMapper.writeValueAsString(any()))
      .thenReturn("{}");

    filter.doFilterInternal(
      request,
      response,
      new MockFilterChain()
    );

    verify(objectMapper).writeValueAsString(any());
  }
}
