package com.tcmp.tiapi.invoice.dto.ti.financeack;

import com.tcmp.tiapi.messaging.model.TINamespace;
import com.tcmp.tiapi.messaging.model.TIOperation;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(
    name = TIOperation.FINANCE_ACK_INVOICE_DETAILS_VALUE,
    namespace = TINamespace.CONTROL)
@XmlAccessorType(XmlAccessType.FIELD)
public class FinanceAckMessage {
  @XmlElement(name = "MessageName", namespace = TINamespace.CONTROL)
  private String messageName;

  @XmlElement(name = "eBankMasterRef", namespace = TINamespace.CONTROL)
  private String eBankMasterRef;

  @XmlElement(name = "eBankEventRef", namespace = TINamespace.CONTROL)
  private String eBankEventRef;

  @XmlElement(name = "MasterRef", namespace = TINamespace.CONTROL)
  private String masterRef;

  @XmlElement(name = "TheirRef", namespace = TINamespace.CONTROL)
  private String theirRef;

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

  @XmlElement(name = "FinancePercent", namespace = TINamespace.CONTROL)
  private String financePercent;

  @XmlElement(name = "EventCode", namespace = TINamespace.CONTROL)
  private String eventCode;

  @XmlElement(name = "BuyerBOB", namespace = TINamespace.CONTROL)
  private String buyerBOB;

  @XmlElement(name = "SCFBuyerRef", namespace = TINamespace.CONTROL)
  private String scfBuyerRef;

  @XmlElement(name = "SellerBOB", namespace = TINamespace.CONTROL)
  private String sellerBOB;

  @XmlElement(name = "SCFSellerRef", namespace = TINamespace.CONTROL)
  private String scfSellerRef;

  @XmlElement(name = "Product", namespace = TINamespace.CONTROL)
  private String product;

  @XmlElement(name = "ProductSubType", namespace = TINamespace.CONTROL)
  private String productSubType;

  @XmlElement(name = "StartDate", namespace = TINamespace.CONTROL)
  private String startDate;

  @XmlElement(name = "DueDate", namespace = TINamespace.CONTROL)
  private String dueDate;

  @XmlElement(name = "FinancingRef", namespace = TINamespace.CONTROL)
  private String financingRef;

  @XmlElement(name = "FinanceDealAmount", namespace = TINamespace.CONTROL)
  private String financeDealAmount;

  @XmlElement(name = "FinanceDealCurrency", namespace = TINamespace.CONTROL)
  private String financeDealCurrency;

  @XmlElement(name = "OutstandingFinanceAmount", namespace = TINamespace.CONTROL)
  private String outstandingFinanceAmount;

  @XmlElement(name = "OutstandingFinanceCurrency", namespace = TINamespace.CONTROL)
  private String outstandingFinanceCurrency;

  @XmlElement(name = "OutstandingAmount", namespace = TINamespace.CONTROL)
  private String outstandingAmount;

  @XmlElement(name = "OutstandingCurrency", namespace = TINamespace.CONTROL)
  private String outstandingCurrency;

  @XmlElement(name = "ReceivedOn", namespace = TINamespace.CONTROL)
  private String receivedOn;

  @XmlElement(name = "MaturityDate", namespace = TINamespace.CONTROL)
  private String maturityDate;

  @XmlElement(name = "FinancePaymentDetails", namespace = TINamespace.CONTROL)
  private FinancePaymentDetails paymentDetails;

  @XmlElement(name = "SenderToReceiverInfo", namespace = TINamespace.CONTROL)
  private String senderToReceiverInfo;

  @XmlElement(name = "InvoiceArray", namespace = TINamespace.CONTROL)
  private List<Invoice> invoiceArray;
}
