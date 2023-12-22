package com.tcmp.tiapi.audit.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcmp.tiapi.audit.model.RequestTraceLog;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.WebUtils;

@RequiredArgsConstructor
@Slf4j
public class RequestTraceLogLoggingFilter extends OncePerRequestFilter {
  private static final int MAX_PAYLOAD_LENGTH = 2_000;

  private final ObjectMapper objectMapper;

  private final Map<HttpMethod, List<String>> methodToIgnoredUris =
      Map.of(HttpMethod.GET, List.of("/", "/health"));

  private AtomicLong startTime;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    boolean isFirstRequest = !isAsyncDispatch(request);
    HttpServletRequest requestWrapper = request;

    if (isFirstRequest && (!(request instanceof ContentCachingRequestWrapper))) {
      requestWrapper = new ContentCachingRequestWrapper(request, MAX_PAYLOAD_LENGTH);
    }

    if (isFirstRequest) {
      beforeRequest();
    }

    try {
      filterChain.doFilter(requestWrapper, response);
    } finally {
      if (!isAsyncStarted(requestWrapper)) {
        afterRequest(requestWrapper, response);
      }
    }
  }

  private void beforeRequest() {
    startTime = new AtomicLong(System.currentTimeMillis());
  }

  private void afterRequest(
      @NonNull HttpServletRequest request, @NonNull HttpServletResponse response) {
    try {
      HttpMethod method = HttpMethod.valueOf(request.getMethod());
      String uri = request.getRequestURI();

      boolean isIgnoredMethodAndUri =
          Optional.ofNullable(methodToIgnoredUris.get(method)).orElse(List.of()).contains(uri);
      if (isIgnoredMethodAndUri) return;

      RequestTraceLog requestTraceLog = buildRequestTraceLog(request, response);
      log.info("{}", objectMapper.writeValueAsString(requestTraceLog));
      // Send To kafka??
    } catch (JsonProcessingException e) {
      log.error("Could not serialize RequestTraceLog");
    }
  }

  public RequestTraceLog buildRequestTraceLog(
      HttpServletRequest request, HttpServletResponse response) {
    long responseTime = System.currentTimeMillis() - startTime.get();
    String messagePayload = getMessagePayload(request);
    String requestBody = messagePayload.replaceAll("[\r\n ]", "");

    return RequestTraceLog.builder()
        .time(Instant.now().toString())
        .status(response.getStatus())
        .requesterIp(request.getRemoteAddr())
        .requestUri(request.getRequestURI())
        .requestMethod(request.getMethod())
        .requestHeaders(extractRequestHeaders(request))
        .requestBody(requestBody)
        .responseTime("%d ms".formatted(responseTime))
        .build();
  }

  protected String getMessagePayload(HttpServletRequest request) {
    ContentCachingRequestWrapper wrapper =
        WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
    if (wrapper == null) return "";

    byte[] buf = wrapper.getContentAsByteArray();
    if (buf.length == 0) return "";

    int length = Math.min(buf.length, MAX_PAYLOAD_LENGTH);
    try {
      return new String(buf, 0, length, wrapper.getCharacterEncoding());
    } catch (UnsupportedEncodingException ex) {
      return "";
    }
  }

  private String extractRequestHeaders(HttpServletRequest request) {
    Enumeration<String> headerNames = request.getHeaderNames();
    StringBuilder result = new StringBuilder();

    while (headerNames.hasMoreElements()) {
      String name = headerNames.nextElement();
      String value = request.getHeader(name);

      result.append(String.format("%s:[%s] ", name, value));
    }

    return result.toString();
  }
}
