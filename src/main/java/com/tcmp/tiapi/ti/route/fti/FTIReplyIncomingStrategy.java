package com.tcmp.tiapi.ti.route.fti;

import com.tcmp.tiapi.ti.dto.response.ServiceResponse;

public interface FTIReplyIncomingStrategy {
  void handleServiceResponse(ServiceResponse serviceResponse);
}
