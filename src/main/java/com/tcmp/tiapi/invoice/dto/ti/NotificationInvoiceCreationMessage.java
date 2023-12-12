package com.tcmp.tiapi.invoice.dto.ti;

import com.tcmp.tiapi.ti.model.TINamespace;
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
@XmlRootElement(name = "tfinvdet")
@XmlAccessorType(XmlAccessType.FIELD)
public class NotificationInvoiceCreationMessage {
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

  @XmlElement(name = "BuyerName", namespace = TINamespace.CONTROL)
  private String buyerName;

  @XmlElement(name = "BuyerAddr1", namespace = TINamespace.CONTROL)
  private String buyerAddr1;

  @XmlElement(name = "BuyerAddr2", namespace = TINamespace.CONTROL)
  private String buyerAddr2;

  @XmlElement(name = "BuyerAddr3", namespace = TINamespace.CONTROL)
  private String buyerAddr3;

  @XmlElement(name = "BuyerAddr4", namespace = TINamespace.CONTROL)
  private String buyerAddr4;

  @XmlElement(name = "BuyerCountry", namespace = TINamespace.CONTROL)
  private String buyerCountry;

  @XmlElement(name = "SellerName", namespace = TINamespace.CONTROL)
  private String sellerName;

  @XmlElement(name = "SellerAddr1", namespace = TINamespace.CONTROL)
  private String sellerAddr1;

  @XmlElement(name = "SellerAddr2", namespace = TINamespace.CONTROL)
  private String sellerAddr2;

  @XmlElement(name = "SellerAddr3", namespace = TINamespace.CONTROL)
  private String sellerAddr3;

  @XmlElement(name = "SellerAddr4", namespace = TINamespace.CONTROL)
  private String sellerAddr4;

  @XmlElement(name = "SellerCountry", namespace = TINamespace.CONTROL)
  private String sellerCountry;

  @XmlElement(name = "ReceivedOn", namespace = TINamespace.CONTROL)
  private String receivedOn;

  @XmlElement(name = "IssueDate", namespace = TINamespace.CONTROL)
  private String issueDate;

  @XmlElement(name = "InvoiceNumber", namespace = TINamespace.CONTROL)
  private String invoiceNumber;

  @XmlElement(name = "FaceValueAmount", namespace = TINamespace.CONTROL)
  private String faceValueAmount;

  @XmlElement(name = "FaceValueCurrency", namespace = TINamespace.CONTROL)
  private String faceValueCurrency;

  @XmlElement(name = "AdjustmentAmount", namespace = TINamespace.CONTROL)
  private String adjustmentAmount;

  @XmlElement(name = "AdjustmentCurrency", namespace = TINamespace.CONTROL)
  private String adjustmentCurrency;

  @XmlElement(name = "AdjustmentDirection", namespace = TINamespace.CONTROL)
  private String adjustmentDirection;

  @XmlElement(name = "OutstandingAmount", namespace = TINamespace.CONTROL)
  private String outstandingAmount;

  @XmlElement(name = "OutstandingCurrency", namespace = TINamespace.CONTROL)
  private String outstandingCurrency;

  @XmlElement(name = "RelatedGoodsOrServices", namespace = TINamespace.CONTROL)
  private String relatedGoodsOrServices;

  @XmlElement(name = "SettlementDate", namespace = TINamespace.CONTROL)
  private String settlementDate;

  @XmlElement(name = "EligibleForFinancing", namespace = TINamespace.CONTROL)
  private String eligibleForFinancing;

  @XmlElement(name = "IneligibilityReason", namespace = TINamespace.CONTROL)
  private String ineligibilityReason;

  @XmlElement(name = "SenderToReceiverInfo", namespace = TINamespace.CONTROL)
  private String senderToReceiverInfo;
}
