package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.invoice.dto.request.InvoiceCreationDTO;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.invoice.messaging.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.messaging.utils.TILocaleNumberFormatUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = TILocaleNumberFormatUtil.class)
public interface InvoiceMapper {

  @Mapping(source = "id", target = "id")
  @Mapping(source = "reference", target = "reference")
  @Mapping(source = "buyerPartyId", target = "buyerPartyId")
  @Mapping(source = "createFinanceEventId", target = "createFinanceEventId")
  @Mapping(source = "batchId", target = "batchId")
  @Mapping(source = "sellerId", target = "sellerId")
  @Mapping(source = "programmeId", target = "programmeId")
  @Mapping(source = "bulkPaymentMasterId", target = "bulkPaymentMasterId")
  @Mapping(source = "subTypeCategory", target = "subTypeCategory")
  @Mapping(source = "programType", target = "programType")
  @Mapping(source = "isApproved", target = "isApproved")
  @Mapping(source = "status", target = "status")
  @Mapping(source = "detailsReceivedOn", target = "detailsReceivedOn")
  @Mapping(source = "settlementDate", target = "settlementDate")
  @Mapping(source = "isDisclosed", target = "isDisclosed")
  @Mapping(source = "isRecourse", target = "isRecourse")
  @Mapping(source = "isDrawDownEligible", target = "isDrawDownEligible")
  @Mapping(source = "preferredCurrencyCode", target = "preferredCurrencyCode")
  @Mapping(source = "isDeferCharged", target = "isDeferCharged")
  @Mapping(source = "eligibilityReasonCode", target = "eligibilityReasonCode")

  @Mapping(source = "faceValueAmount", target = "faceValue.amount")
  @Mapping(source = "faceValueCurrencyCode", target = "faceValue.currency")
  @Mapping(source = "totalPaidAmount", target = "totalPaid.amount")
  @Mapping(source = "totalPaidCurrencyCode", target = "totalPaid.currency")
  @Mapping(source = "outstandingAmount", target = "outstanding.amount")
  @Mapping(source = "outstandingAmountCurrencyCode", target = "outstanding.currency")
  @Mapping(source = "advanceAvailableAmount", target = "advanceAvailable.amount")
  @Mapping(source = "advanceAvailableCurrencyCode", target = "advanceAvailable.currency")
  @Mapping(source = "advanceAvailableEquivalentAmount", target = "advanceAvailableEquivalent.amount")
  @Mapping(source = "advanceAvailableEquivalentCurrencyCode", target = "advanceAvailableEquivalent.currency")
  @Mapping(source = "discountAdvanceAmount", target = "discountAdvance.amount")
  @Mapping(source = "discountAdvanceAmountCurrencyCode", target = "discountAdvance.currency")
  @Mapping(source = "discountDealAmount", target = "discountDeal.amount")
  @Mapping(source = "discountDealAmountCurrencyCode", target = "discountDeal.currency")

  @Mapping(source = "detailsNotesForCustomer", target = "detailsNotesForCustomer")
  @Mapping(source = "securityDetails", target = "securityDetails")
  @Mapping(source = "taxDetails", target = "taxDetails")
  InvoiceDTO mapEntityToDTO(InvoiceMaster invoiceMaster);

  @Mapping(source = "context.customer", target = "context.customer")
  @Mapping(source = "context.theirReference", target = "context.theirReference")
  @Mapping(source = "context.branch", target = "context.branch")
  @Mapping(source = "context.behalfOfBranch", target = "context.behalfOfBranch")
  @Mapping(source = "anchorParty", target = "anchorParty")
  @Mapping(source = "programme", target = "programme")
  @Mapping(source = "seller", target = "seller")
  @Mapping(source = "buyer", target = "buyer")
  @Mapping(source = "receivedOn", target = "receivedOn")
  @Mapping(source = "invoiceNumber", target = "invoiceNumber")
  @Mapping(source = "issueDate", target = "issueDate")
  @Mapping(source = "faceValue.amount", target = "faceValue.amount", numberFormat = "#,###.00")
  @Mapping(source = "faceValue.currency", target = "faceValue.currency")
  @Mapping(source = "outstandingAmount.amount", target = "outstandingAmount.amount", numberFormat = "#,###.00")
  @Mapping(source = "outstandingAmount", target = "outstandingAmount")
  @Mapping(source = "settlementDate", target = "settlementDate")
  CreateInvoiceEventMessage mapDTOToFTIMessage(InvoiceCreationDTO invoiceCreationDTO);
}
