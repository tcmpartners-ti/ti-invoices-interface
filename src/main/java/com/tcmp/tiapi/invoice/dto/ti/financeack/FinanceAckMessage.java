package com.tcmp.tiapi.invoice.dto.ti.financeack;


import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "tfinvfindet")
@XmlAccessorType(XmlAccessType.FIELD)
public class FinanceAckMessage {
  @XmlElement(name = "MessageName")
  private String messageName;

  @XmlElement(name = "eBankMasterRef")
  private String eBankMasterRef;

  @XmlElement(name = "eBankEventRef")
  private String eBankEventRef;

  @XmlElement(name = "MasterRef")
  private String masterRef;

  @XmlElement(name = "TheirRef")
  private String theirRef;

  @XmlElement(name = "EventRef")
  private String eventRef;

  @XmlElement(name = "BehalfOfBranch")
  private String behalfOfBranch;

  @XmlElement(name = "SBB")
  private String sbb;

  @XmlElement(name = "MBE")
  private String mbe;

  @XmlElement(name = "Programme")
  private String programme;

  @XmlElement(name = "ProgrammeTypeCode")
  private String programmeTypeCode;

  @XmlElement(name = "BuyerIdentifier")
  private String buyerIdentifier;

  @XmlElement(name = "SellerIdentifier")
  private String sellerIdentifier;

  @XmlElement(name = "AnchorPartyCustomerMnemonic")
  private String anchorPartyCustomerMnemonic;

  @XmlElement(name = "CounterpartyCustomerMnemonic")
  private String counterpartyCustomerMnemonic;

  @XmlElement(name = "SellerName")
  private String sellerName;

  @XmlElement(name = "SellerAddr1")
  private String sellerAddr1;

  @XmlElement(name = "SellerAddr2")
  private String sellerAddr2;

  @XmlElement(name = "SellerAddr3")
  private String sellerAddr3;

  @XmlElement(name = "SellerAddr4")
  private String sellerAddr4;

  @XmlElement(name = "SellerCountry")
  private String sellerCountry;

  @XmlElement(name = "BuyerName")
  private String buyerName;

  @XmlElement(name = "BuyerAddr1")
  private String buyerAddr1;

  @XmlElement(name = "BuyerAddr2")
  private String buyerAddr2;

  @XmlElement(name = "BuyerAddr3")
  private String buyerAddr3;

  @XmlElement(name = "BuyerAddr4")
  private String buyerAddr4;

  @XmlElement(name = "BuyerCountry")
  private String buyerCountry;

  @XmlElement(name = "FinancePercent")
  private String financePercent;

  @XmlElement(name = "EventCode")
  private String eventCode;

  @XmlElement(name = "BuyerBOB")
  private String buyerBOB;

  @XmlElement(name = "SCFBuyerRef")
  private String scfBuyerRef;

  @XmlElement(name = "SellerBOB")
  private String sellerBOB;

  @XmlElement(name = "SCFSellerRef")
  private String scfSellerRef;

  @XmlElement(name = "Product")
  private String product;

  @XmlElement(name = "ProductSubType")
  private String productSubType;

  @XmlElement(name = "StartDate")
  private String startDate;

  @XmlElement(name = "DueDate")
  private String dueDate;

  @XmlElement(name = "FinancingRef")
  private String financingRef;

  @XmlElement(name = "FinanceDealAmount")
  private String financeDealAmount;

  @XmlElement(name = "FinanceDealCurrency")
  private String financeDealCurrency;

  @XmlElement(name = "OutstandingFinanceAmount")
  private String outstandingFinanceAmount;

  @XmlElement(name = "OutstandingFinanceCurrency")
  private String outstandingFinanceCurrency;

  @XmlElement(name = "OutstandingAmount")
  private String outstandingAmount;

  @XmlElement(name = "OutstandingCurrency")
  private String outstandingCurrency;

  @XmlElement(name = "ReceivedOn")
  private String receivedOn;

  @XmlElement(name = "MaturityDate")
  private String maturityDate;

  @XmlElement(name = "FinancePaymentDetails")
  private FinancePaymentDetails paymentDetails;

  @XmlElement(name = "SenderToReceiverInfo")
  private String senderToReceiverInfo;

  @XmlElement(name = "InvoiceArray")
  private List<Invoice> invoiceArray;
}
