package com.tcmp.tiapi.invoice.dto.ti;

import com.tcmp.tiapi.messaging.model.TINamespace;
import com.tcmp.tiapi.messaging.model.TIOperation;
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
@XmlRootElement(name = TIOperation.DUE_INVOICE_DETAILS_VALUE, namespace = TINamespace.CONTROL)
@XmlAccessorType(XmlAccessType.FIELD)
public class CreateDueInvoiceEventMessage {
  @XmlElement(name = "MessageName", namespace = TINamespace.CONTROL)
  private String messageName;

  @XmlElement(name = "eBankMasterRef", namespace = TINamespace.CONTROL)
  private String eBankMasterRef;

  @XmlElement(name = "eBankEventRef", namespace = TINamespace.CONTROL)
  private String eBankEventRef;

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

  @XmlElement(name = "BankComment", namespace = TINamespace.CONTROL)
  private String bankComment;

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

  @XmlElement(name = "SCFBuyerRef", namespace = TINamespace.CONTROL)
  private String scfBuyerRef;

  @XmlElement(name = "SellerBOB", namespace = TINamespace.CONTROL)
  private String sellerBOB;

  @XmlElement(name = "SCFSellerRef", namespace = TINamespace.CONTROL)
  private String scfSellerRef;

  @XmlElement(name = "ReceivedOn", namespace = TINamespace.CONTROL)
  private String receivedOn;

  @XmlElement(name = "IssueDate", namespace = TINamespace.CONTROL)
  private String issueDate;

  @XmlElement(name = "PaymentValueDate", namespace = TINamespace.CONTROL)
  private String paymentValueDate;

  @XmlElement(name = "InvoiceNumber", namespace = TINamespace.CONTROL)
  private String invoiceNumber;

  @XmlElement(name = "PaymentAmount", namespace = TINamespace.CONTROL)
  private String paymentAmount;

  @XmlElement(name = "PaymentCurrency", namespace = TINamespace.CONTROL)
  private String paymentCurrency;

  @XmlElement(name = "OutstandingAmount", namespace = TINamespace.CONTROL)
  private String outstandingAmount;

  @XmlElement(name = "OutstandingCurrency", namespace = TINamespace.CONTROL)
  private String outstandingCurrency;

  @XmlElement(name = "EligibleForFinancing", namespace = TINamespace.CONTROL)
  private String eligibleForFinancing;

  @XmlElement(name = "InvoiceStatusCode", namespace = TINamespace.CONTROL)
  private String invoiceStatusCode;

  @XmlElement(name = "NotesForCustomer", namespace = TINamespace.CONTROL)
  private String notesForCustomer;

  @XmlElement(name = "NotesForBuyer", namespace = TINamespace.CONTROL)
  private String notesForBuyer;


}
