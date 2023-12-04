package com.tcmp.tiapi.titoapigee.paymentexecution.dto.response;

import java.io.Serializable;
import lombok.Builder;

@Builder
public record TransferResponseError(
    String title,
    String detail,
    String instance,
    String type,
    String resource,
    String component,
    String backend)
    implements Serializable {}
