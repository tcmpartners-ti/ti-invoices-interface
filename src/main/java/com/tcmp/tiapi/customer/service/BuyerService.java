package com.tcmp.tiapi.customer.service;

import com.tcmp.tiapi.customer.dto.request.SearchBuyerInvoicesParams;
import com.tcmp.tiapi.customer.repository.CounterPartyRepository;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.repository.InvoiceSpecifications;
import com.tcmp.tiapi.program.mapper.ProgramMapper;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.model.InterestTier;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.program.repository.InterestTierRepository;
import com.tcmp.tiapi.program.repository.ProgramRepository;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResult;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResultMeta;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuyerService {

  private final Clock clock;
  private final InterestTierRepository interestTierRepository;
  private final CustomerRepository customerRepository;
  private final CounterPartyRepository counterPartyRepository;
  private final InvoiceRepository invoiceRepository;
  private final ProgramRepository programRepository;
  private final InvoiceMapper invoiceMapper;
  private final ProgramMapper programMapper;

  public PaginatedResult<InvoiceDTO> getBuyerInvoices(
      String buyerMnemonic, SearchBuyerInvoicesParams searchParams, PageParams pageParams) {
    checkIfBuyerExistsOrThrowNotFound(buyerMnemonic);

    Page<InvoiceMaster> buyerInvoicesPage =
        invoiceRepository.findAll(
            InvoiceSpecifications.filterByBuyerMnemonicAndStatus(
                buyerMnemonic, searchParams.status(), LocalDate.now(clock)),
            PageRequest.of(pageParams.getPage(), pageParams.getSize()));
    Set<Long> programIds =
        buyerInvoicesPage.stream().map(InvoiceMaster::getProgrammeId).collect(Collectors.toSet());
    Set<Long> buyerIds =
        buyerInvoicesPage.stream().map(InvoiceMaster::getBuyerId).collect(Collectors.toSet());

    Map<String, BigDecimal> buyerProgramRates =
        interestTierRepository.findAllByProgrammeIdInAndBuyerIdIn(programIds, buyerIds).stream()
            .collect(
                Collectors.toMap(
                    this::buildProgramBuyerKeys,
                    t -> Optional.ofNullable(t.getRate()).orElse(BigDecimal.ZERO),
                        (old, actual) -> actual));

    return PaginatedResult.<InvoiceDTO>builder()
        .data(invoiceMapper.mapEntitiesToDTOs(buyerInvoicesPage.getContent(), buyerProgramRates))
        .meta(PaginatedResultMeta.from(buyerInvoicesPage))
        .build();
  }

  public PaginatedResult<ProgramDTO> getBuyerProgramsByMnemonic(
      String buyerMnemonic, PageParams pageParams) {
    if (!customerRepository.existsByIdMnemonic(buyerMnemonic)) {
      throw new NotFoundHttpException(
          String.format("Could not find customer with mnemonic %s.", buyerMnemonic));
    }

    Page<Program> programsPage =
        programRepository.findAllByCustomerMnemonic(
            buyerMnemonic, PageRequest.of(pageParams.getPage(), pageParams.getSize()));

    Set<Long> programIds = programsPage.stream().map(Program::getPk).collect(Collectors.toSet());
    Map<Long, BigDecimal> programsInterests =
        interestTierRepository.findByProgrammeIdIn(programIds).stream()
            .collect(
                Collectors.toMap(
                    t -> t.getInterest().getProgramId(),
                    t -> Optional.ofNullable(t.getRate()).orElse(BigDecimal.ZERO)));

    return PaginatedResult.<ProgramDTO>builder()
        .data(programMapper.mapEntitiesToDTOs(programsPage.getContent(), programsInterests))
        .meta(
            PaginatedResultMeta.builder()
                .isLastPage(programsPage.isLast())
                .totalPages(programsPage.getTotalPages())
                .totalItems(programsPage.getTotalElements())
                .build())
        .build();
  }

  private void checkIfBuyerExistsOrThrowNotFound(String buyerMnemonic) {
    if (!counterPartyRepository.counterPartyIsBuyer(buyerMnemonic)) {
      throw new NotFoundHttpException(
          String.format("Could not find a buyer with mnemonic %s.", buyerMnemonic));
    }
  }

  private String buildProgramBuyerKeys(InterestTier tier) {
    Long programId = tier.getInterest().getMap().getProgramId();
    Long buyerId = tier.getInterest().getMap().getCounterPartyId();

    return String.format("%d:%d", programId, buyerId);
  }
}
