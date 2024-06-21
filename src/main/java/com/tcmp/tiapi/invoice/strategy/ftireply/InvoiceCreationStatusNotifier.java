package com.tcmp.tiapi.invoice.strategy.ftireply;

import com.tcmp.tiapi.ti.dto.response.ServiceResponse;

public interface InvoiceCreationStatusNotifier {
  void notify(ServiceResponse serviceResponse);
}
