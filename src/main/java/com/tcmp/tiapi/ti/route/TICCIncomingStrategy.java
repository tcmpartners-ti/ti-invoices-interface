package com.tcmp.tiapi.ti.route;

import com.tcmp.tiapi.ti.model.requests.AckServiceRequest;

public interface TICCIncomingStrategy {
  void handleServiceRequest(AckServiceRequest<?> request);
}
