package com.tcmp.tiapi.invoice.dto.ti.cancel;

import com.tcmp.tiapi.ti.dto.TINamespace;
import com.tcmp.tiapi.ti.dto.TIOperationTag;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = TIOperationTag.INVOICE_CANCELLATION_RESULT, namespace = TINamespace.CONTROL)
@XmlAccessorType(XmlAccessType.FIELD)
public class CancelInvoiceEventMessage {

  @XmlElement(name = "MessageName", namespace = TINamespace.CONTROL)
  private String messageName;

  @XmlElement(name = "eBankMasterRef", namespace = TINamespace.CONTROL, nillable = true)
  private String eBankMasterRef;

  @XmlElement(name = "MasterRef", namespace = TINamespace.CONTROL)
  private String masterRef;

  @XmlElement(name = "EventRef", namespace = TINamespace.CONTROL)
  private String eventRef;

  @XmlElement(name = "BehalfOfBranch", namespace = TINamespace.CONTROL)
  private String behalfOfBranch;

  @XmlElement(name = "SBB", namespace = TINamespace.CONTROL)
  private String sbb;

  @XmlElement(name = "MBE", namespace = TINamespace.CONTROL)
  private String mbe;

  @XmlElement(name = "Programme", namespace = TINamespace.CONTROL)
  private String programme;

  @XmlElement(name = "ProgrammeTypeCode", namespace = TINamespace.CONTROL)
  private String programmeTypeCode;

  @XmlElement(name = "BuyerIdentifier", namespace = TINamespace.CONTROL)
  private String buyerIdentifier;

  @XmlElement(name = "SellerIdentifier", namespace = TINamespace.CONTROL)
  private String sellerIdentifier;

  @XmlElement(name = "AnchorPartyCustomerMnemonic", namespace = TINamespace.CONTROL)
  private String anchorPartyCustomerMnemonic;

  @XmlElement(name = "CounterpartyCustomerMnemonic", namespace = TINamespace.CONTROL)
  private String counterpartyCustomerMnemonic;

  @XmlElement(name = "BuyerBOB", namespace = TINamespace.CONTROL)
  private String buyerBOB;

  @XmlElement(name = "SCFBuyerRef", namespace = TINamespace.CONTROL, nillable = true)
  private String scfBuyerRef;

  @XmlElement(name = "SellerBOB", namespace = TINamespace.CONTROL)
  private String sellerBOB;

  @XmlElement(name = "SCFSellerRef", namespace = TINamespace.CONTROL, nillable = true)
  private String scfSellerRef;

  @XmlElement(name = "ReceivedOn", namespace = TINamespace.CONTROL)
  private String receivedOn;

  @XmlElement(name = "InvoiceNumber", namespace = TINamespace.CONTROL)
  private String invoiceNumber;

  @XmlElement(name = "CancellationDate", namespace = TINamespace.CONTROL)
  private String cancellationDate;

  @XmlElement(name = "ReasonsForCancellation", namespace = TINamespace.CONTROL, nillable = true)
  private String reasonsForCancellation;
}
