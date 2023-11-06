package com.tcmp.tiapi.titoapigee.operationalgateway.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum InvoiceEmailEvent {
  PROCESSED("Procesamiento"),
  FINANCED("Anticipo"),
  SETTLED("Vencimiento"),
  CREDITED("Consumo crédito proveedor por descuento");

  private final String value;
}
