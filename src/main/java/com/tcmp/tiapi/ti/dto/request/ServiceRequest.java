package com.tcmp.tiapi.ti.dto.request;

import com.tcmp.tiapi.customer.dto.ti.Account;
import com.tcmp.tiapi.customer.dto.ti.Customer;
import com.tcmp.tiapi.customer.dto.ti.CustomerItemRequest;
import com.tcmp.tiapi.invoice.dto.ti.creation.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.dto.ti.finance.FinanceBuyerCentricInvoiceEventMessage;
import com.tcmp.tiapi.ti.dto.TINamespace;
import com.tcmp.tiapi.ti.dto.TIOperation;
import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "ServiceRequest")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlSeeAlso({
  CreateInvoiceEventMessage.class,
  FinanceBuyerCentricInvoiceEventMessage.class,
  Customer.class,
  Account.class
})
public class ServiceRequest<T> {
  // Missing namespaces
  @XmlAttribute(namespace = TINamespace.SCHEMA)
  private String schemaNamespace;

  @XmlAttribute(name = "xmlns", namespace = TINamespace.CONTROL)
  private String controlNamespace;

  @XmlElement(name = "RequestHeader")
  private RequestHeader header;

  @XmlElements({
    @XmlElement(
        name = TIOperation.CREATE_INVOICE_VALUE,
        type = CreateInvoiceEventMessage.class,
        namespace = TINamespace.MESSAGES),
    @XmlElement(
        name = TIOperation.FINANCE_INVOICE_VALUE,
        type = FinanceBuyerCentricInvoiceEventMessage.class,
        namespace = TINamespace.MESSAGES),
    @XmlElement(
        name = TIOperation.CREATE_CUSTOMER_VALUE,
        type = Customer.class,
        namespace = TINamespace.MESSAGES),
    @XmlElement(
        name = TIOperation.CREATE_ACCOUNT_VALUE,
        type = Account.class,
        namespace = TINamespace.MESSAGES),
    @XmlElement(
        name = "ItemRequest",
        type = CustomerItemRequest.class,
        namespace = TINamespace.MESSAGES)
  })
  private T body;
}
