package com.tcmp.tiapi.titoapigee.operationalgateway.dto.response;

import java.util.List;

public record NotificationsResponse(
  List<NotificationInfo> data
) {
}
