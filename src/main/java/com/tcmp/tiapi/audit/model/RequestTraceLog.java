package com.tcmp.tiapi.audit.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RequestTraceLog(
    String time,
    String guid,
    String channel,
    String medium,
    String app,
    String session,
    int status,
    String requesterIp,
    String requestUri,
    String requestMethod,
    String requestBody,
    String requestHeaders,
    String responseBody,
    String responseTime) {}
