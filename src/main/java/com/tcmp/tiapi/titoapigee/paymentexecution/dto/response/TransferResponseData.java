package com.tcmp.tiapi.titoapigee.paymentexecution.dto.response;

import lombok.Builder;

@Builder
public record TransferResponseData(String status, String paymentId, String creationDateTime) {}
