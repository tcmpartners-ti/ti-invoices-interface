package com.tcmp.tiapi.titoapigee.corporateloan.dto.request;

import lombok.Builder;

@Builder
public record Customer(
    String customerId, String documentNumber, String documentType, String fullName) {}
