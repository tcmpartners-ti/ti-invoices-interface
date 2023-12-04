package com.tcmp.tiapi.titoapigee.operationalgateway.dto.request;

import java.util.List;
import lombok.Builder;

@Builder
public record NotificationsRequest(
    Flow flow, Requester requester, List<Recipient> additionalRecipient, Template template) {}
