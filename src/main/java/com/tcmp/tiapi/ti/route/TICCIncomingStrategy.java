package com.tcmp.tiapi.ti.route;

import com.tcmp.tiapi.ti.dto.request.AckServiceRequest;

public interface TICCIncomingStrategy {
  void handleServiceRequest(AckServiceRequest<?> request);
}
