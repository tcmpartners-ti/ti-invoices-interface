package com.tcmp.tiapi.titoapigee.operationalgateway.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum InvoiceEmailEvent {
  PROCESSED("Procesamiento"),
  FINANCED("Anticipo"),
  SETTLED("Vencimiento"),
  CREDITED("Consumo crédito proveedor por descuento"),
  CANCELLED("Solicitud de cancelación"),
  POSTED("Carga");

  private final String value;
}
