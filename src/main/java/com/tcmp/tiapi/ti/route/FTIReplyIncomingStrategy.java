package com.tcmp.tiapi.ti.route;

import com.tcmp.tiapi.ti.dto.response.ServiceResponse;

public interface FTIReplyIncomingStrategy {
  void handleServiceResponse(ServiceResponse serviceResponse);
}
