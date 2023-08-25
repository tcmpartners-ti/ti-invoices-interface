package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.invoice.dto.InvoiceCreationRowCSV;
import com.tcmp.tiapi.invoice.dto.request.InvoiceCreationDTO;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.invoice.messaging.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.messaging.utils.TILocaleNumberFormatUtil;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(uses = TILocaleNumberFormatUtil.class, imports = {BigDecimal.class})
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

  @Mapping(
    target = "faceValue.amount",
    expression = "java(mapNullableBigDecimal(invoiceMaster.getFaceValueAmount()))"
  )
  @Mapping(source = "faceValueCurrencyCode", target = "faceValue.currency")
  @Mapping(
    target = "totalPaid.amount",
    expression = "java(mapNullableBigDecimal(invoiceMaster.getTotalPaidAmount()))"
  )
  @Mapping(source = "totalPaidCurrencyCode", target = "totalPaid.currency")
  @Mapping(
    target = "outstanding.amount",
    expression = "java(mapNullableBigDecimal(invoiceMaster.getFaceValueAmount()))"
  )
  @Mapping(source = "outstandingAmountCurrencyCode", target = "outstanding.currency")
  @Mapping(
    target = "advanceAvailable.amount",
    expression = "java(mapNullableBigDecimal(invoiceMaster.getAdvanceAvailableAmount()))"
  )
  @Mapping(source = "advanceAvailableCurrencyCode", target = "advanceAvailable.currency")
  @Mapping(
    target = "advanceAvailableEquivalent.amount",
    expression = "java(mapNullableBigDecimal(invoiceMaster.getAdvanceAvailableEquivalentAmount()))"
  )
  @Mapping(source = "advanceAvailableEquivalentCurrencyCode", target = "advanceAvailableEquivalent.currency")
  @Mapping(
    target = "discountAdvance.amount",
    expression = "java(mapNullableBigDecimal(invoiceMaster.getDiscountAdvanceAmount()))"
  )
  @Mapping(source = "discountAdvanceAmountCurrencyCode", target = "discountAdvance.currency")
  @Mapping(
    target = "discountDeal.amount",
    expression = "java(mapNullableBigDecimal(invoiceMaster.getDiscountDealAmount()))"
  )
  @Mapping(source = "discountDealAmountCurrencyCode", target = "discountDeal.currency")

  @Mapping(source = "detailsNotesForCustomer", target = "detailsNotesForCustomer")
  @Mapping(source = "securityDetails", target = "securityDetails")
  @Mapping(source = "taxDetails", target = "taxDetails")
  InvoiceDTO mapEntityToDTO(InvoiceMaster invoiceMaster);

  default BigDecimal mapNullableBigDecimal(BigDecimal input) {
    return input != null
      ? input.divide(new BigDecimal(100))
      : null;
  }

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
  @Mapping(target = "invoiceApproved", expression = "java(invoiceCreationDTO.getInvoiceApproved() ? \"Y\" : \"N\")")
  CreateInvoiceEventMessage mapDTOToFTIMessage(InvoiceCreationDTO invoiceCreationDTO);

  @Mapping(source = "customerMnemonic", target = "context.customer")
  @Mapping(source = "theirReference", target = "context.theirReference")
  @Mapping(source = "branch", target = "context.branch")
  @Mapping(source = "behalfOfBranch", target = "context.behalfOfBranch")
  @Mapping(source = "anchorPartyMnemonic", target = "anchorParty")
  @Mapping(source = "programmeId", target = "programme")
  @Mapping(source = "sellerId", target = "seller")
  @Mapping(source = "buyerId", target = "buyer")
  @Mapping(source = "receivedOn", target = "receivedOn", dateFormat = "yyyy-MM-dd")
  @Mapping(source = "invoiceNumber", target = "invoiceNumber")
  @Mapping(source = "issueDate", target = "issueDate", dateFormat = "yyyy-MM-dd")
  @Mapping(source = "faceValueAmount", target = "faceValue.amount", numberFormat = "#,###.00")
  @Mapping(source = "faceValueCurrency", target = "faceValue.currency")
  @Mapping(source = "outstandingAmount", target = "outstandingAmount.amount", numberFormat = "#,###.00")
  @Mapping(source = "outstandingCurrency", target = "outstandingAmount.currency")
  @Mapping(source = "settlementDate", target = "settlementDate", dateFormat = "yyyy-MM-dd")
  CreateInvoiceEventMessage mapCSVRowToFTIMessage(InvoiceCreationRowCSV invoiceCreationRowCSV);
}
