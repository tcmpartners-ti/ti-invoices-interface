package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.customer.model.CounterParty;
import com.tcmp.tiapi.invoice.dto.InvoiceCreationRowCSV;
import com.tcmp.tiapi.invoice.dto.request.InvoiceCreationDTO;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.invoice.messaging.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.messaging.utils.TILocaleNumberFormatUtil;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper(uses = TILocaleNumberFormatUtil.class, imports = {BigDecimal.class, MonetaryAmountUtils.class})
public interface InvoiceMapper {

  @Mapping(source = "invoiceMaster.id", target = "id")
  @Mapping(source = "invoiceMaster.reference", target = "invoiceNumber")
  @Mapping(source = "invoiceMaster.buyerPartyId", target = "buyerPartyId")
  @Mapping(source = "invoiceMaster.createFinanceEventId", target = "createFinanceEventId")
  @Mapping(source = "invoiceMaster.batchId", target = "batchId")

  @Mapping(source = "buyer.id", target = "buyer.id")
  @Mapping(source = "buyer.mnemonic", target = "buyer.mnemonic")
  @Mapping(source = "buyer.name", target = "buyer.name")
  @Mapping(source = "seller.id", target = "seller.id")
  @Mapping(source = "seller.mnemonic", target = "seller.mnemonic")
  @Mapping(source = "seller.name", target = "seller.name")
  @Mapping(source = "program.id", target = "program.id")
  @Mapping(source = "program.description", target = "program.description")

  @Mapping(source = "invoiceMaster.bulkPaymentMasterId", target = "bulkPaymentMasterId")
  @Mapping(source = "invoiceMaster.subTypeCategory", target = "subTypeCategory")
  @Mapping(source = "invoiceMaster.programType", target = "programType")
  @Mapping(source = "invoiceMaster.isApproved", target = "isApproved")
  @Mapping(source = "invoiceMaster.status", target = "status")
  @Mapping(source = "invoiceMaster.detailsReceivedOn", target = "detailsReceivedOn")
  @Mapping(source = "invoiceMaster.settlementDate", target = "settlementDate")
  @Mapping(source = "invoiceMaster.isDisclosed", target = "isDisclosed")
  @Mapping(source = "invoiceMaster.isRecourse", target = "isRecourse")
  @Mapping(source = "invoiceMaster.isDrawDownEligible", target = "isDrawDownEligible")
  @Mapping(source = "invoiceMaster.preferredCurrencyCode", target = "preferredCurrencyCode")
  @Mapping(source = "invoiceMaster.isDeferCharged", target = "isDeferCharged")
  @Mapping(source = "invoiceMaster.eligibilityReasonCode", target = "eligibilityReasonCode")

  @Mapping(target = "faceValue.amount", expression = "java(MonetaryAmountUtils.convertCentsToDollars(invoiceMaster.getFaceValueAmount()))")
  @Mapping(source = "invoiceMaster.faceValueCurrencyCode", target = "faceValue.currency")
  @Mapping(target = "totalPaid.amount", expression = "java(MonetaryAmountUtils.convertCentsToDollars(invoiceMaster.getTotalPaidAmount()))")
  @Mapping(source = "invoiceMaster.totalPaidCurrencyCode", target = "totalPaid.currency")
  @Mapping(target = "outstanding.amount", expression = "java(MonetaryAmountUtils.convertCentsToDollars(invoiceMaster.getFaceValueAmount()))")
  @Mapping(source = "invoiceMaster.outstandingAmountCurrencyCode", target = "outstanding.currency")
  @Mapping(target = "advanceAvailable.amount", expression = "java(MonetaryAmountUtils.convertCentsToDollars(invoiceMaster.getAdvanceAvailableAmount()))")
  @Mapping(source = "invoiceMaster.advanceAvailableCurrencyCode", target = "advanceAvailable.currency")
  @Mapping(target = "advanceAvailableEquivalent.amount", expression = "java(MonetaryAmountUtils.convertCentsToDollars(invoiceMaster.getAdvanceAvailableEquivalentAmount()))")
  @Mapping(source = "invoiceMaster.advanceAvailableEquivalentCurrencyCode", target = "advanceAvailableEquivalent.currency")
  @Mapping(target = "discountAdvance.amount", expression = "java(MonetaryAmountUtils.convertCentsToDollars(invoiceMaster.getDiscountAdvanceAmount()))")
  @Mapping(source = "invoiceMaster.discountAdvanceAmountCurrencyCode", target = "discountAdvance.currency")
  @Mapping(target = "discountDeal.amount", expression = "java(MonetaryAmountUtils.convertCentsToDollars(invoiceMaster.getDiscountDealAmount()))")
  @Mapping(source = "invoiceMaster.discountDealAmountCurrencyCode", target = "discountDeal.currency")

  @Mapping(source = "invoiceMaster.detailsNotesForCustomer", target = "detailsNotesForCustomer")
  @Mapping(source = "invoiceMaster.securityDetails", target = "securityDetails")
  @Mapping(source = "invoiceMaster.taxDetails", target = "taxDetails")
  InvoiceDTO mapEntityToDTO(InvoiceMaster invoiceMaster, CounterParty buyer, CounterParty seller, Program program);

  @Mapping(source = "context.customer", target = "context.customer")
  @Mapping(source = "context.theirReference", target = "context.theirReference")
  @Mapping(source = "context.branch", target = "context.branch")
  @Mapping(source = "context.behalfOfBranch", target = "context.behalfOfBranch")
  @Mapping(target = "batchId", expression = "java(null)")
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

  @Mapping(source = "invoiceCreationRowCSV.customerMnemonic", target = "context.customer")
  @Mapping(source = "invoiceCreationRowCSV.theirReference", target = "context.theirReference")
  @Mapping(source = "invoiceCreationRowCSV.branch", target = "context.branch")
  @Mapping(source = "invoiceCreationRowCSV.behalfOfBranch", target = "context.behalfOfBranch")
  @Mapping(source = "invoiceCreationRowCSV.anchorPartyMnemonic", target = "anchorParty")
  @Mapping(source = "batchId", target = "batchId")
  @Mapping(source = "invoiceCreationRowCSV.programmeId", target = "programme")
  @Mapping(source = "invoiceCreationRowCSV.sellerId", target = "seller")
  @Mapping(source = "invoiceCreationRowCSV.buyerId", target = "buyer")
  @Mapping(source = "invoiceCreationRowCSV.receivedOn", target = "receivedOn", dateFormat = "yyyy-MM-dd")
  @Mapping(source = "invoiceCreationRowCSV.invoiceNumber", target = "invoiceNumber")
  @Mapping(source = "invoiceCreationRowCSV.issueDate", target = "issueDate", dateFormat = "yyyy-MM-dd")
  @Mapping(source = "invoiceCreationRowCSV.faceValueAmount", target = "faceValue.amount", numberFormat = "#,###.00")
  @Mapping(source = "invoiceCreationRowCSV.faceValueCurrency", target = "faceValue.currency")
  @Mapping(source = "invoiceCreationRowCSV.outstandingAmount", target = "outstandingAmount.amount", numberFormat = "#,###.00")
  @Mapping(source = "invoiceCreationRowCSV.outstandingCurrency", target = "outstandingAmount.currency")
  @Mapping(source = "invoiceCreationRowCSV.settlementDate", target = "settlementDate", dateFormat = "yyyy-MM-dd")
  @Mapping(target = "invoiceApproved", expression = "java(\"Y\")")
  CreateInvoiceEventMessage mapCSVRowToFTIMessage(InvoiceCreationRowCSV invoiceCreationRowCSV, String batchId);

  default  List<InvoiceDTO> mapEntitiesToDTOs(
    List<InvoiceMaster> invoiceMasters,
    Map<Long, CounterParty> idToCounterparty,
    Map<Long, Program> idToProgram
  ) {
    List<InvoiceDTO> invoicesDTOs = new ArrayList<>(invoiceMasters.size());
    for (InvoiceMaster invoiceMaster : invoiceMasters) {
      invoicesDTOs.add(mapEntityToDTO(
        invoiceMaster,
        idToCounterparty.get(invoiceMaster.getBuyerId()),
        idToCounterparty.get(invoiceMaster.getSellerId()),
        idToProgram.get(invoiceMaster.getProgrammeId())
      ));
    }

    return invoicesDTOs;
  }
}
