package com.tcmp.tiapi.titoapigee.operationalgateway.dto.request;

import lombok.Builder;

import java.util.List;

@Builder
public record NotificationsRequest(
    Flow flow, Requester requester, List<Recipient> additionalRecipient, Template template) {}
