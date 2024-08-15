package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.customer.mapper.CounterPartyMapper;
import com.tcmp.tiapi.invoice.dto.InvoiceCreationRowCSV;
import com.tcmp.tiapi.invoice.dto.request.InvoiceCreationDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceFinancingDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceNumberDTO;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.invoice.dto.ti.creation.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.dto.ti.finance.*;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.program.mapper.ProgramMapper;
import com.tcmp.tiapi.shared.mapper.CurrencyAmountMapper;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import com.tcmp.tiapi.shared.utils.StringMappingUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
    uses = {CurrencyAmountMapper.class, CounterPartyMapper.class, ProgramMapper.class},
    componentModel = MappingConstants.ComponentModel.SPRING,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    builder = @Builder(disableBuilder = true),
    imports = {
      BigDecimal.class,
      List.class,
      LocalDate.class,
      DateTimeFormatter.class,
      MonetaryAmountUtils.class,
      StringMappingUtils.class
    })
public abstract class InvoiceMapper {
  protected static final String DTO_DATE_FORMAT = "dd-MM-yyyy";

  @Autowired protected CurrencyAmountMapper currencyMapper;
  @Autowired protected CounterPartyMapper counterPartyMapper;
  @Autowired protected ProgramMapper programMapper;

  @Mapping(source = "invoice.id", target = "id")
  @Mapping(
      target = "invoiceNumber",
      expression = "java(StringMappingUtils.trimNullable(invoice.getReference()))")
  @Mapping(source = "invoice.buyerPartyId", target = "buyerPartyId")
  @Mapping(source = "invoice.createFinanceEventId", target = "createFinanceEventId")
  @Mapping(
      target = "batchId",
      expression = "java(StringMappingUtils.trimNullable(invoice.getBatchId()))")
  @Mapping(source = "invoice.bulkPaymentMasterId", target = "bulkPaymentMasterId")
  @Mapping(source = "invoice.subTypeCategory", target = "subTypeCategory")
  @Mapping(source = "invoice.programType", target = "programType")
  @Mapping(source = "invoice.isApproved", target = "isApproved")
  @Mapping(source = "invoice.status", target = "status")
  @Mapping(source = "invoice.detailsReceivedOn", target = "detailsReceivedOn")
  @Mapping(source = "invoice.settlementDate", target = "settlementDate")
  @Mapping(source = "invoice.productMaster.contractDate", target = "issueDate")
  @Mapping(source = "invoice.isDisclosed", target = "isDisclosed")
  @Mapping(source = "invoice.isRecourse", target = "isRecourse")
  @Mapping(source = "invoice.isDrawDownEligible", target = "isDrawDownEligible")
  @Mapping(
      target = "preferredCurrencyCode",
      expression = "java(StringMappingUtils.trimNullable(invoice.getPreferredCurrencyCode()))")
  @Mapping(source = "invoice.isDeferCharged", target = "isDeferCharged")
  @Mapping(source = "invoice.eligibilityReasonCode", target = "eligibilityReasonCode")
  @Mapping(source = "invoice.detailsNotesForCustomer", target = "detailsNotesForCustomer")
  @Mapping(source = "invoice.securityDetails", target = "securityDetails")
  @Mapping(source = "invoice.taxDetails", target = "taxDetails")
  @Mapping(
      target = "faceValue",
      expression =
          "java(currencyMapper.mapToDto(invoice.getFaceValueAmount(), invoice.getFaceValueCurrencyCode()))")
  @Mapping(
      target = "totalPaid",
      expression =
          "java(currencyMapper.mapToDto(invoice.getTotalPaidAmount(), invoice.getTotalPaidCurrencyCode()))")
  @Mapping(
      target = "outstanding",
      expression =
          "java(currencyMapper.mapToDto(invoice.getOutstandingAmount(), invoice.getOutstandingAmountCurrencyCode()))")
  @Mapping(
      target = "advanceAvailable",
      expression =
          "java(currencyMapper.mapToDto(invoice.getAdvanceAvailableAmount(), invoice.getAdvanceAvailableCurrencyCode()))")
  @Mapping(
      target = "advanceAvailableEquivalent",
      expression =
          "java(currencyMapper.mapToDto(invoice.getAdvanceAvailableEquivalentAmount(), invoice.getAdvanceAvailableEquivalentCurrencyCode()))")
  @Mapping(
      target = "discountAdvance",
      expression =
          "java(currencyMapper.mapToDto(invoice.getDiscountAdvanceAmount(), invoice.getDiscountAdvanceAmountCurrencyCode()))")
  @Mapping(
      target = "discountDeal",
      expression =
          "java(currencyMapper.mapToDto(invoice.getDiscountDealAmount(), invoice.getDiscountDealAmountCurrencyCode()))")
  @Mapping(
      target = "buyer",
      expression = "java(counterPartyMapper.mapEntityToInvoiceDTO(invoice.getBuyer()))")
  @Mapping(
      target = "seller",
      expression = "java(counterPartyMapper.mapEntityToInvoiceDTO(invoice.getSeller()))")
  @Mapping(
      target = "programme",
      expression =
          "java(programMapper.mapEntityToInvoiceDTO(invoice.getProgram(), programInterestRate))")
  public abstract InvoiceDTO mapEntityToDTO(InvoiceMaster invoice, BigDecimal programInterestRate);

  @Mapping(source = "context.customer", target = "context.customer")
  @Mapping(source = "context.theirReference", target = "context.theirReference")
  @Mapping(target = "context.ourReference", ignore = true)
  @Mapping(source = "context.behalfOfBranch", target = "context.branch")
  @Mapping(source = "context.behalfOfBranch", target = "context.behalfOfBranch")
  @Mapping(target = "batchId", expression = "java(null)")
  @Mapping(source = "anchorParty", target = "anchorParty")
  @Mapping(source = "anchorAccount", target = "extraData.financeAccount")
  @Mapping(source = "programme", target = "programme")
  @Mapping(source = "seller", target = "seller")
  @Mapping(source = "buyer", target = "buyer")
  @Mapping(source = "invoiceNumber", target = "invoiceNumber")
  @Mapping(source = "faceValue.amount", target = "faceValue.amount", numberFormat = "#,###.00")
  @Mapping(source = "faceValue.currency", target = "faceValue.currency")
  @Mapping(
      source = "outstandingAmount.amount",
      target = "outstandingAmount.amount",
      numberFormat = "#,###.00")
  @Mapping(source = "outstandingAmount", target = "outstandingAmount")
  @Mapping(source = "issueDate", target = "issueDate", dateFormat = DTO_DATE_FORMAT)
  @Mapping(source = "settlementDate", target = "settlementDate", dateFormat = DTO_DATE_FORMAT)
  @Mapping(target = "invoiceApproved", constant = "Y")
  public abstract CreateInvoiceEventMessage mapDTOToFTIMessage(
      InvoiceCreationDTO invoiceCreationDTO);

  @Mapping(source = "invoiceRow.customer", target = "context.customer")
  @Mapping(source = "invoiceRow.theirReference", target = "context.theirReference")
  @Mapping(source = "invoiceRow.behalfOfBranch", target = "context.branch")
  @Mapping(source = "invoiceRow.behalfOfBranch", target = "context.behalfOfBranch")
  @Mapping(source = "invoiceRow.anchorParty", target = "anchorParty")
  @Mapping(source = "invoiceRow.anchorAccount", target = "extraData.financeAccount")
  @Mapping(source = "fileUUid", target = "extraData.fileUuid")
  @Mapping(source = "batchId", target = "batchId")
  @Mapping(source = "invoiceRow.programme", target = "programme")
  @Mapping(source = "invoiceRow.seller", target = "seller")
  @Mapping(source = "invoiceRow.buyer", target = "buyer")
  @Mapping(source = "invoiceRow.invoiceNumber", target = "invoiceNumber")
  @Mapping(
      source = "invoiceRow.faceValueAmount",
      target = "faceValue.amount",
      numberFormat = "#,###.00")
  @Mapping(source = "invoiceRow.faceValueCurrency", target = "faceValue.currency")
  @Mapping(
      source = "invoiceRow.outstandingAmount",
      target = "outstandingAmount.amount",
      numberFormat = "#,###.00")
  @Mapping(source = "invoiceRow.outstandingCurrency", target = "outstandingAmount.currency")
  @Mapping(source = "invoiceRow.issueDate", target = "issueDate", dateFormat = DTO_DATE_FORMAT)
  @Mapping(
      source = "invoiceRow.settlementDate",
      target = "settlementDate",
      dateFormat = DTO_DATE_FORMAT)
  @Mapping(target = "invoiceApproved", constant = "Y")
  public abstract CreateInvoiceEventMessage mapCSVRowToFTIMessage(
      InvoiceCreationRowCSV invoiceRow, String batchId, String fileUUid);

  @Mapping(source = "context.customer", target = "context.customer")
  @Mapping(source = "context.behalfOfBranch", target = "context.behalfOfBranch")
  @Mapping(source = "context.behalfOfBranch", target = "context.branch")
  @Mapping(source = "context.theirReference", target = "context.theirReference")
  @Mapping(source = "context.theirReference", target = "theirReference")
  @Mapping(target = "context.ourReference", ignore = true)
  @Mapping(source = "programme", target = "programme")
  @Mapping(source = "seller", target = "seller")
  // Mapping(source = "sellerAccount", target = "extraFinancingData.financeSellerAccount")
  @Mapping(source = "buyer", target = "buyer")
  @Mapping(source = "anchorParty", target = "anchorParty")
  @Mapping(source = "financeCurrency", target = "financeCurrency")
  @Mapping(source = "financePercent", target = "financePercent")
  @Mapping(source = "maturityDate", target = "maturityDate", dateFormat = DTO_DATE_FORMAT)
  @Mapping(source = "financeDate", target = "financeDate", dateFormat = DTO_DATE_FORMAT)
  @Mapping(
      target = "invoiceNumbersContainer.invoiceNumbers",
      expression = "java(List.of(mapDTOToInvoiceNumbers(invoiceFinancingDTO.getInvoice())))")
  public abstract void mapFinancingDTOToFTIMessage(
      InvoiceFinancingDTO invoiceFinancingDTO,
      @MappingTarget FinanceInvoiceEventMessage financeInvoiceEventMessage);

  public FinanceBuyerCentricInvoiceEventMessage mapFinancingBuyerCentricDTOToFTIMessage(
      InvoiceFinancingDTO invoiceFinancingDTO) {

    FinanceBuyerCentricInvoiceEventMessage buyerCentricFinance =
        new FinanceBuyerCentricInvoiceEventMessage();
    mapFinancingDTOToFTIMessage(invoiceFinancingDTO, buyerCentricFinance);
    buyerCentricFinance.setExtraFinancingData(
        ExtraFinancingData.builder()
            .financeSellerAccount(invoiceFinancingDTO.getSellerAccount())
            .build());
    return buyerCentricFinance;
  }

  public FinanceSellerCentricInvoiceEventMessage mapFinancingSellerCentricDTOToFTIMessage(
      InvoiceFinancingDTO invoiceFinancingDTO) {
    FinanceSellerCentricInvoiceEventMessage sellerCentricFinance =
        new FinanceSellerCentricInvoiceEventMessage();
    mapFinancingDTOToFTIMessage(invoiceFinancingDTO, sellerCentricFinance);
    return sellerCentricFinance;
  }

  @Mapping(source = "number", target = "invoiceNumber")
  @Mapping(source = "issueDate", target = "issueDate", dateFormat = DTO_DATE_FORMAT)
  @Mapping(source = "outstanding.amount", target = "outstandingAmount")
  @Mapping(source = "outstanding.currency", target = "outstandingAmountCurrency")
  protected abstract InvoiceNumbers mapDTOToInvoiceNumbers(InvoiceNumberDTO invoiceNumberDTO);

  public List<InvoiceDTO> mapEntitiesToDTOs(
      List<InvoiceMaster> invoices, Map<String, BigDecimal> sellerProgramRates) {
    List<InvoiceDTO> invoicesDTOs = new ArrayList<>(invoices.size());
    for (InvoiceMaster invoice : invoices) {
      BigDecimal rate = getInterestOrDefault(invoice, sellerProgramRates);
      invoicesDTOs.add(mapEntityToDTO(invoice, rate));
    }

    return invoicesDTOs;
  }

  private BigDecimal getInterestOrDefault(
      InvoiceMaster invoice, Map<String, BigDecimal> sellerProgramRates) {
    String key = String.format("%d:%d", invoice.getProgrammeId(), invoice.getSellerId());

    return Optional.ofNullable(sellerProgramRates.get(key)).orElse(BigDecimal.ZERO);
  }
}
