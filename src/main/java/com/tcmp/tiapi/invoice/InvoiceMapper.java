package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.customer.mapper.CounterPartyMapper;
import com.tcmp.tiapi.customer.model.CounterParty;
import com.tcmp.tiapi.invoice.dto.InvoiceCreationRowCSV;
import com.tcmp.tiapi.invoice.dto.request.InvoiceCreationDTO;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.invoice.messaging.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.program.ProgramMapper;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.shared.mapper.CurrencyAmountMapper;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import com.tcmp.tiapi.shared.utils.StringMappingUtils;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Mapper(
  uses = {
    CurrencyAmountMapper.class,
    CounterPartyMapper.class,
    ProgramMapper.class
  },
  componentModel = MappingConstants.ComponentModel.SPRING,
  injectionStrategy = InjectionStrategy.CONSTRUCTOR,
  imports = {
    BigDecimal.class,
    MonetaryAmountUtils.class,
    StringMappingUtils.class
  }
)
public abstract class InvoiceMapper {
  @Autowired protected CurrencyAmountMapper currencyMapper;
  @Autowired protected CounterPartyMapper counterPartyMapper;
  @Autowired protected ProgramMapper programMapper;

  @Mapping(source = "invoice.id", target = "id")
  @Mapping(target = "invoiceNumber", expression = "java(StringMappingUtils.trimNullable(invoice.getReference()))")
  @Mapping(source = "invoice.buyerPartyId", target = "buyerPartyId")
  @Mapping(source = "invoice.createFinanceEventId", target = "createFinanceEventId")
  @Mapping(target = "batchId", expression = "java(StringMappingUtils.trimNullable(invoice.getBatchId()))")

  @Mapping(target = "buyer", expression = "java(counterPartyMapper.mapEntityToInvoiceDTO(buyer))")
  @Mapping(target = "seller", expression = "java(counterPartyMapper.mapEntityToInvoiceDTO(seller))")
  @Mapping(target = "programme", expression = "java(programMapper.mapEntityToInvoiceDTO(program))")

  @Mapping(source = "invoice.bulkPaymentMasterId", target = "bulkPaymentMasterId")
  @Mapping(source = "invoice.subTypeCategory", target = "subTypeCategory")
  @Mapping(source = "invoice.programType", target = "programType")
  @Mapping(source = "invoice.isApproved", target = "isApproved")
  @Mapping(source = "invoice.status", target = "status")
  @Mapping(source = "invoice.detailsReceivedOn", target = "detailsReceivedOn")
  @Mapping(source = "invoice.settlementDate", target = "settlementDate")
  @Mapping(source = "invoice.isDisclosed", target = "isDisclosed")
  @Mapping(source = "invoice.isRecourse", target = "isRecourse")
  @Mapping(source = "invoice.isDrawDownEligible", target = "isDrawDownEligible")
  @Mapping(target = "preferredCurrencyCode", expression = "java(StringMappingUtils.trimNullable(invoice.getPreferredCurrencyCode()))")
  @Mapping(source = "invoice.isDeferCharged", target = "isDeferCharged")
  @Mapping(source = "invoice.eligibilityReasonCode", target = "eligibilityReasonCode")

  @Mapping(target = "faceValue", expression = "java(currencyMapper.mapToDto(invoice.getFaceValueAmount(), invoice.getFaceValueCurrencyCode()))")
  @Mapping(target = "totalPaid", expression = "java(currencyMapper.mapToDto(invoice.getTotalPaidAmount(), invoice.getTotalPaidCurrencyCode()))")
  @Mapping(target = "outstanding", expression = "java(currencyMapper.mapToDto(invoice.getOutstandingAmount(), invoice.getOutstandingAmountCurrencyCode()))")
  @Mapping(target = "advanceAvailable", expression = "java(currencyMapper.mapToDto(invoice.getAdvanceAvailableAmount(), invoice.getAdvanceAvailableCurrencyCode()))")
  @Mapping(target = "advanceAvailableEquivalent", expression = "java(currencyMapper.mapToDto(invoice.getAdvanceAvailableEquivalentAmount(), invoice.getAdvanceAvailableEquivalentCurrencyCode()))")
  @Mapping(target = "discountAdvance", expression = "java(currencyMapper.mapToDto(invoice.getDiscountAdvanceAmount(), invoice.getDiscountAdvanceAmountCurrencyCode()))")
  @Mapping(target = "discountDeal", expression = "java(currencyMapper.mapToDto(invoice.getDiscountDealAmount(), invoice.getDiscountDealAmountCurrencyCode()))")

  @Mapping(source = "invoice.detailsNotesForCustomer", target = "detailsNotesForCustomer")
  @Mapping(source = "invoice.securityDetails", target = "securityDetails")
  @Mapping(source = "invoice.taxDetails", target = "taxDetails")
  public abstract InvoiceDTO mapEntityToDTO(InvoiceMaster invoice, CounterParty buyer, CounterParty seller, Program program);

  @Mapping(source = "context.customer", target = "context.customer")
  @Mapping(source = "context.theirReference", target = "context.theirReference")
  @Mapping(source = "context.behalfOfBranch", target = "context.behalfOfBranch")
  @Mapping(target = "batchId", expression = "java(null)")
  @Mapping(source = "anchorParty", target = "anchorParty")
  @Mapping(source = "programme", target = "programme")
  @Mapping(source = "seller", target = "seller")
  @Mapping(source = "buyer", target = "buyer")
  @Mapping(source = "invoiceNumber", target = "invoiceNumber")
  @Mapping(source = "issueDate", target = "issueDate")
  @Mapping(source = "faceValue.amount", target = "faceValue.amount", numberFormat = "#,###.00")
  @Mapping(source = "faceValue.currency", target = "faceValue.currency")
  @Mapping(source = "outstandingAmount.amount", target = "outstandingAmount.amount", numberFormat = "#,###.00")
  @Mapping(source = "outstandingAmount", target = "outstandingAmount")
  @Mapping(source = "settlementDate", target = "settlementDate")
  @Mapping(target = "invoiceApproved", expression = "java(invoiceCreationDTO.getInvoiceApproved() != null && invoiceCreationDTO.getInvoiceApproved() ? \"Y\" : \"N\")")
  public abstract CreateInvoiceEventMessage mapDTOToFTIMessage(InvoiceCreationDTO invoiceCreationDTO);

  @Mapping(source = "invoiceCreationRowCSV.customerMnemonic", target = "context.customer")
  @Mapping(source = "invoiceCreationRowCSV.theirReference", target = "context.theirReference")
  @Mapping(source = "invoiceCreationRowCSV.behalfOfBranch", target = "context.behalfOfBranch")
  @Mapping(source = "invoiceCreationRowCSV.anchorPartyMnemonic", target = "anchorParty")
  @Mapping(source = "batchId", target = "batchId")
  @Mapping(source = "invoiceCreationRowCSV.programmeId", target = "programme")
  @Mapping(source = "invoiceCreationRowCSV.sellerId", target = "seller")
  @Mapping(source = "invoiceCreationRowCSV.buyerId", target = "buyer")
  @Mapping(source = "invoiceCreationRowCSV.invoiceNumber", target = "invoiceNumber")
  @Mapping(source = "invoiceCreationRowCSV.issueDate", target = "issueDate", dateFormat = "yyyy-MM-dd")
  @Mapping(source = "invoiceCreationRowCSV.faceValueAmount", target = "faceValue.amount", numberFormat = "#,###.00")
  @Mapping(source = "invoiceCreationRowCSV.faceValueCurrency", target = "faceValue.currency")
  @Mapping(source = "invoiceCreationRowCSV.outstandingAmount", target = "outstandingAmount.amount", numberFormat = "#,###.00")
  @Mapping(source = "invoiceCreationRowCSV.outstandingCurrency", target = "outstandingAmount.currency")
  @Mapping(source = "invoiceCreationRowCSV.settlementDate", target = "settlementDate", dateFormat = "yyyy-MM-dd")
  @Mapping(target = "invoiceApproved", expression = "java(\"Y\")")
  public abstract CreateInvoiceEventMessage mapCSVRowToFTIMessage(InvoiceCreationRowCSV invoiceCreationRowCSV, String batchId);

  public List<InvoiceDTO> mapEntitiesToDTOs(
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
