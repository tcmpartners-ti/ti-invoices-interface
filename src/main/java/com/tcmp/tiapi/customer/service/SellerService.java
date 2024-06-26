package com.tcmp.tiapi.customer.service;

import com.tcmp.tiapi.customer.dto.response.OutstandingBalanceDTO;
import com.tcmp.tiapi.customer.dto.response.SearchSellerInvoicesParams;
import com.tcmp.tiapi.customer.model.Customer;
import com.tcmp.tiapi.customer.repository.CounterPartyRepository;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.model.ProductMasterStatus;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.repository.InvoiceSpecifications;
import com.tcmp.tiapi.program.ProgramMapper;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.model.InterestTier;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.program.repository.InterestTierRepository;
import com.tcmp.tiapi.program.repository.ProgramRepository;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResult;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResultMeta;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import jakarta.annotation.Nullable;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerService {
  private final Clock clock;

  private final InterestTierRepository interestTierRepository;
  private final CustomerRepository customerRepository;
  private final CounterPartyRepository counterPartyRepository;
  private final InvoiceRepository invoiceRepository;
  private final ProgramRepository programRepository;
  private final InvoiceMapper invoiceMapper;
  private final ProgramMapper programMapper;

  public PaginatedResult<InvoiceDTO> getSellerInvoices(
      String sellerMnemonic, SearchSellerInvoicesParams searchParams, PageParams pageParams) {
    checkIfSellerExistsOrThrowNotFound(sellerMnemonic);

    Page<InvoiceMaster> sellerInvoicesPage =
        invoiceRepository.findAll(
            InvoiceSpecifications.filterBySellerMnemonicAndStatus(
                sellerMnemonic, searchParams.status(), LocalDate.now(clock)),
            PageRequest.of(pageParams.getPage(), pageParams.getSize()));

    Set<Long> programIds =
        sellerInvoicesPage.stream().map(InvoiceMaster::getProgrammeId).collect(Collectors.toSet());
    Set<Long> sellerIds =
        sellerInvoicesPage.stream().map(InvoiceMaster::getSellerId).collect(Collectors.toSet());

    Map<String, BigDecimal> sellerProgramRates =
        interestTierRepository.findAllByProgrammeIdInAndSellerIdIn(programIds, sellerIds).stream()
            .collect(
                Collectors.toMap(
                    this::buildProgramSellerKeys,
                    t -> Optional.ofNullable(t.getRate()).orElse(BigDecimal.ZERO),
                    // Deal with duplicates
                    (old, actual) -> actual));

    return PaginatedResult.<InvoiceDTO>builder()
        .data(invoiceMapper.mapEntitiesToDTOs(sellerInvoicesPage.getContent(), sellerProgramRates))
        .meta(PaginatedResultMeta.from(sellerInvoicesPage))
        .build();
  }

  private String buildProgramSellerKeys(InterestTier tier) {
    Long programId = tier.getInterest().getMap().getProgramId();
    Long sellerId = tier.getInterest().getMap().getCounterPartyId();

    return String.format("%d:%d", programId, sellerId);
  }

  public PaginatedResult<ProgramDTO> getSellerProgramsByMnemonic(
      String sellerMnemonic, PageParams pageParams) {
    checkIfSellerExistsOrThrowNotFound(sellerMnemonic);

    PageRequest pageable = PageRequest.of(pageParams.getPage(), pageParams.getSize());
    Page<Program> programsPage =
        programRepository.findAllBySellerMnemonic(sellerMnemonic, pageable);

    Set<Long> sellerIds = counterPartyRepository.findSellerIdsByMnemonic(sellerMnemonic);
    Map<Long, BigDecimal> programsInterests = getProgramsInterests(programsPage, sellerIds);

    return PaginatedResult.<ProgramDTO>builder()
        .data(programMapper.mapEntitiesToDTOs(programsPage.getContent(), programsInterests))
        .meta(PaginatedResultMeta.from(programsPage))
        .build();
  }

  public PaginatedResult<ProgramDTO> getSellerProgramsByCif(
      String sellerCif, PageParams pageParams) {
    Customer seller =
        customerRepository
            .findFirstByNumber(sellerCif)
            .orElseThrow(
                () ->
                    new NotFoundHttpException(
                        String.format("Could not find a seller with cif %s.", sellerCif)));

    PageRequest pageable = PageRequest.of(pageParams.getPage(), pageParams.getSize());
    Page<Program> programsPage = programRepository.findAllBySellerCif(sellerCif, pageable);
    Set<Long> sellerIds = counterPartyRepository.findSellerIdsByMnemonic(seller.getGfcus());

    Map<Long, BigDecimal> programsInterests = getProgramsInterests(programsPage, sellerIds);
    return PaginatedResult.<ProgramDTO>builder()
        .data(programMapper.mapEntitiesToDTOs(programsPage.getContent(), programsInterests))
        .meta(PaginatedResultMeta.from(programsPage))
        .build();
  }

  private Map<Long, BigDecimal> getProgramsInterests(
      Page<Program> programsPage, Set<Long> sellerIds) {
    Set<Long> programIds = programsPage.stream().map(Program::getPk).collect(Collectors.toSet());

    return interestTierRepository
        .findAllByProgrammeIdInAndSellerIdIn(programIds, sellerIds)
        .stream()
        .collect(
            Collectors.toMap(
                t -> t.getInterest().getProgramId(),
                t -> Optional.ofNullable(t.getRate()).orElse(BigDecimal.ZERO)));
  }

  public OutstandingBalanceDTO getSellerOutstandingBalanceByMnemonic(
      String sellerMnemonic, @Nullable String buyerMnemonic) {
    checkIfSellerExistsOrThrowNotFound(sellerMnemonic);
    checkIfBuyerExistsOrThrowNotFound(buyerMnemonic);

    if (buyerMnemonic != null) {
      boolean isSellerRelatedToBuyer =
          customerRepository.totalRelationsWithBuyer(sellerMnemonic, buyerMnemonic) > 0;
      if (!isSellerRelatedToBuyer) {
        throw new NotFoundHttpException(
            String.format(
                "Could not find any invoices linked to seller %s and buyer %s.",
                sellerMnemonic, buyerMnemonic));
      }

      boolean sellerHasInvoicesLinkedToBuyer =
          sellerHasInvoicesWithBuyer(sellerMnemonic, buyerMnemonic);
      if (!sellerHasInvoicesLinkedToBuyer) {
        return new OutstandingBalanceDTO(BigDecimal.ZERO);
      }
    }

    BigDecimal notFinancedOutstandingBalance =
        invoiceRepository
            .getNotFinancedOutstandingBalanceBySellerMnemonic(sellerMnemonic, buyerMnemonic)
            .map(MonetaryAmountUtils::convertCentsToDollars)
            .orElse(BigDecimal.ZERO);

    BigDecimal financedOutstandingBalance =
        invoiceRepository
            .getFinancedOutstandingBalanceBySellerMnemonic(sellerMnemonic, buyerMnemonic)
            .map(MonetaryAmountUtils::convertCentsToDollars)
            .orElse(BigDecimal.ZERO);

    BigDecimal totalOutstandingBalance =
        notFinancedOutstandingBalance.add(financedOutstandingBalance);

    return new OutstandingBalanceDTO(totalOutstandingBalance);
  }

  private void checkIfSellerExistsOrThrowNotFound(String sellerMnemonic) {
    if (!counterPartyRepository.counterPartyIsSeller(sellerMnemonic)) {
      throw new NotFoundHttpException(
          String.format("Could not find a seller with mnemonic %s.", sellerMnemonic));
    }
  }

  private void checkIfBuyerExistsOrThrowNotFound(@Nullable String buyerMnemonic) {
    if (buyerMnemonic == null) return;

    if (!counterPartyRepository.counterPartyIsBuyer(buyerMnemonic)) {
      throw new NotFoundHttpException(
          String.format("Could not find a buyer with mnemonic %s.", buyerMnemonic));
    }
  }

  private boolean sellerHasInvoicesWithBuyer(String sellerMnemonic, String buyerMnemonic) {
    return invoiceRepository.existsBySellerMnemonicAndBuyerMnemonicAndStatusAndProductMasterStatus(
        sellerMnemonic, buyerMnemonic, 'O', ProductMasterStatus.LIV);
  }
}
