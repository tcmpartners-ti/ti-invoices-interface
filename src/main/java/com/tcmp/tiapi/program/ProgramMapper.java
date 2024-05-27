package com.tcmp.tiapi.program;

import com.tcmp.tiapi.customer.dto.ti.ScfBuyerOrSeller;
import com.tcmp.tiapi.customer.dto.ti.ScfRelationship;
import com.tcmp.tiapi.invoice.dto.response.InvoiceProgramDTO;
import com.tcmp.tiapi.program.dto.csv.ProgramCreationCsvRow;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.dto.ti.ScfProgramme;
import com.tcmp.tiapi.program.model.Interest;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.shared.mapper.CurrencyAmountMapper;
import com.tcmp.tiapi.shared.utils.MapperUtils;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import com.tcmp.tiapi.shared.utils.StringMappingUtils;
import com.tcmp.tiapi.ti.TIServiceRequestWrapper;
import com.tcmp.tiapi.ti.dto.CustomerRole;
import com.tcmp.tiapi.ti.dto.TIOperation;
import com.tcmp.tiapi.ti.dto.TIService;
import com.tcmp.tiapi.ti.dto.request.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
    imports = {MonetaryAmountUtils.class, StringMappingUtils.class, CustomerRole.class},
    componentModel = MappingConstants.ComponentModel.SPRING,
    injectionStrategy = InjectionStrategy.CONSTRUCTOR,
    uses = {CurrencyAmountMapper.class, MapperUtils.class})
public abstract class ProgramMapper {
  private static final String DATE_FORMAT = "dd-MM-yyyy";

  @Autowired private TIServiceRequestWrapper wrapper;
  @Autowired protected CurrencyAmountMapper currencyAmountMapper;

  @Mapping(source = "id", target = "id")
  @Mapping(source = "description", target = "description")
  @Mapping(source = "customer.id.mnemonic", target = "customer.mnemonic")
  @Mapping(source = "customer.type", target = "customer.commercialTradeCode")
  @Mapping(source = "startDate", target = "startDate")
  @Mapping(source = "expiryDate", target = "expiryDate")
  @Mapping(source = "type", target = "type")
  @Mapping(source = "status", target = "status")
  @Mapping(
      target = "creditLimit",
      expression =
          "java(currencyAmountMapper.mapToDto(program.getAvailableLimitAmount(), program.getAvailableLimitCurrencyCode()))")
  @Mapping(
      source = "extension.extraFinancingDays",
      target = "extraFinancingDays",
      defaultValue = "0")
  @Mapping(target = "interestRate", expression = "java(mapInterestsToRate(program.getInterests()))")
  public abstract ProgramDTO mapEntityToDTO(Program program);

  public abstract List<ProgramDTO> mapEntitiesToDTOs(List<Program> programs);

  @Mapping(target = "interestRate", expression = "java(mapInterestsToRate(program.getInterests()))")
  @Mapping(target = "extraFinancingDays", source = "extension.extraFinancingDays")
  public abstract InvoiceProgramDTO mapEntityToInvoiceDTO(Program program);

  public BigDecimal mapInterestsToRate(List<Interest> interests) {
    if (interests == null) return BigDecimal.ZERO;

    return interests.stream()
        .filter(interest -> interest.getScfMap() == null)
        .findFirst()
        .map(Interest::getTier)
        .map(tiers -> tiers.get(0).getRate())
        .orElse(BigDecimal.ZERO);
  }

  @Mapping(target = "maintenanceType", constant = "F")
  @Mapping(target = "maintainedInBackOffice", constant = "false")
  @Mapping(target = "id", source = "programmeId")
  @Mapping(target = "description", source = "programmeDescription")
  @Mapping(target = "customer.sourceBankingBusiness", source = "sourceBankingBusiness")
  @Mapping(
      target = "customer.mnemonic",
      expression = "java(mapCustomerMnemonic(programCreationCsvRow))")
  @Mapping(target = "type", expression = "java(mapType(row.getProgrammeType()))")
  @Mapping(target = "subType", source = "programmeSubtype")
  @Mapping(target = "creditLimit.currency", source = "creditLimitCurrency")
  @Mapping(target = "creditLimit.amount", source = "creditLimitAmount")
  @Mapping(target = "status", constant = "A")
  @Mapping(target = "startDate", source = "programmeStartDate", dateFormat = DATE_FORMAT)
  @Mapping(target = "expiryDate", source = "programmeExpiryDate", dateFormat = DATE_FORMAT)
  @Mapping(target = "narrative", constant = "Narrative")
  @Mapping(target = "financeProductType", ignore = true)
  @Mapping(target = "invoiceUploadedBy", expression = "java(CustomerRole.BUYER)")
  @Mapping(target = "financeRequestedBy", expression = "java(CustomerRole.SELLER)")
  @Mapping(target = "financeDebitParty", expression = "java(CustomerRole.BUYER)")
  @Mapping(target = "financeToParty", expression = "java(CustomerRole.SELLER)")
  @Mapping(target = "buyerAcceptanceRequired", constant = "true")
  @Mapping(target = "behalfOfBranch", source = "branch")
  @Mapping(target = "parentGuarantorExists", constant = "false")
  public abstract ScfProgramme mapRowToProgram(ProgramCreationCsvRow row);

  protected ScfProgramme.Type mapType(String programmeType) {
    return switch (programmeType) {
      case "B" -> ScfProgramme.Type.BUYER_CENTRIC;
      case "S" -> ScfProgramme.Type.SELLER_CENTRIC;
      default -> null;
    };
  }

  protected String mapCustomerMnemonic(ProgramCreationCsvRow row) {
    return switch (row.getProgrammeType()) {
      case "B" -> row.getBuyerMnemonic();
      case "S" -> row.getSellerMnemonic();
      default -> null;
    };
  }

  @Mapping(target = "branch", source = "branch")
  @Mapping(target = "maintenanceType", constant = "F")
  @Mapping(target = "maintainedInBackOffice", constant = "false")
  @Mapping(target = "programme", source = "programmeId")
  @Mapping(target = "buyerOrSeller", source = "buyerMnemonic")
  @Mapping(target = "role", expression = "java(CustomerRole.BUYER)")
  @Mapping(target = "customer.sourceBankingBusiness", source = "sourceBankingBusiness")
  @Mapping(target = "customer.mnemonic", source = "buyerMnemonic")
  @Mapping(target = "name", source = "buyerName")
  @Mapping(target = "transferMethod", constant = "GW")
  @Mapping(target = "language", constant = "GB")
  @Mapping(target = "status", constant = "A")
  @Mapping(target = "invoiceLimit.currency", source = "buyerInvoiceLimitCurrency")
  @Mapping(target = "invoiceLimit.amount", source = "buyerInvoiceLimitAmount")
  public abstract ScfBuyerOrSeller mapRowToBuyer(ProgramCreationCsvRow row);

  @Mapping(target = "branch", source = "branch")
  @Mapping(target = "maintenanceType", constant = "F")
  @Mapping(target = "maintainedInBackOffice", constant = "false")
  @Mapping(target = "programme", source = "programmeId")
  @Mapping(target = "buyerOrSeller", source = "sellerMnemonic")
  @Mapping(target = "role", expression = "java(CustomerRole.SELLER)")
  @Mapping(target = "customer.sourceBankingBusiness", source = "sourceBankingBusiness")
  @Mapping(target = "customer.mnemonic", source = "sellerMnemonic")
  @Mapping(target = "name", source = "sellerName")
  @Mapping(target = "transferMethod", constant = "GW")
  @Mapping(target = "language", constant = "GB")
  @Mapping(target = "status", constant = "A")
  @Mapping(target = "invoiceLimit.currency", source = "sellerInvoiceLimitCurrency")
  @Mapping(target = "invoiceLimit.amount", source = "sellerInvoiceLimitAmount")
  public abstract ScfBuyerOrSeller mapRowToSeller(ProgramCreationCsvRow row);

  @Mapping(target = "maintenanceType", constant = "F")
  @Mapping(target = "maintainedInBackOffice", constant = "false")
  @Mapping(target = "makerCheckerRequired", constant = "false")
  @Mapping(target = "programme", source = "programmeId")
  @Mapping(target = "seller", source = "sellerMnemonic")
  @Mapping(target = "buyer", source = "buyerMnemonic")
  @Mapping(target = "customerBuyerLimit.currency", source = "customerBuyerLimitCurrency")
  @Mapping(target = "customerBuyerLimit.amount", source = "customerBuyerLimitAmount")
  @Mapping(target = "buyerPercent", source = "buyerPercent")
  @Mapping(target = "maximumPeriod.period", source = "maximumPeriod")
  @Mapping(target = "maximumPeriod.days", source = "maximumPeriodDays")
  @Mapping(target = "recourse", constant = "false")
  @Mapping(target = "disclosed", constant = "false")
  @Mapping(target = "calculateEligibilityFromDateOfReceipt", constant = "false")
  public abstract ScfRelationship mapRowToRelationship(ProgramCreationCsvRow row);

  public MultiItemServiceRequest mapRowToItemRequest(ProgramCreationCsvRow row) {
    ServiceRequest<ScfProgramme> program =
        wrapper.wrapRequest(
            TIService.TRADE_INNOVATION,
            TIOperation.CREATE_PROGRAMME,
            ReplyFormat.STATUS,
            null,
            mapRowToProgram(row));
    ServiceRequest<ScfBuyerOrSeller> buyer =
        wrapper.wrapRequest(
            TIService.TRADE_INNOVATION,
            TIOperation.CREATE_BUYER_OR_SELLER,
            ReplyFormat.STATUS,
            null,
            mapRowToBuyer(row));
    ServiceRequest<ScfBuyerOrSeller> seller =
        wrapper.wrapRequest(
            TIService.TRADE_INNOVATION,
            TIOperation.CREATE_BUYER_OR_SELLER,
            ReplyFormat.STATUS,
            null,
            mapRowToSeller(row));
    ServiceRequest<ScfRelationship> relationship =
        wrapper.wrapRequest(
            TIService.TRADE_INNOVATION,
            TIOperation.CREATE_BUYER_SELLER_RELATIONSHIP,
            ReplyFormat.STATUS,
            null,
            mapRowToRelationship(row));

    RequestHeader requestHeader =
        RequestHeader.builder()
            .service(TIService.TRADE_INNOVATION_BULK.getValue())
            .operation(TIOperation.ITEM.getValue())
            .replyFormat(ReplyFormat.STATUS.getValue())
            .noOverride("N")
            .correlationId(null)
            .credentials(Credentials.builder().name("TI_INTERFACE").build())
            .build();
    List<ItemRequest> itemRequests = new ArrayList<>();
    itemRequests.add(new ItemRequest(program));
    itemRequests.add(new ItemRequest(buyer));
    itemRequests.add(new ItemRequest(seller));
    itemRequests.add(new ItemRequest(relationship));

    return new MultiItemServiceRequest(requestHeader, itemRequests);
  }
}
