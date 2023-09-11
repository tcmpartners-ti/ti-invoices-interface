package com.tcmp.tiapi.customer.service;

import com.tcmp.tiapi.customer.model.CounterParty;
import com.tcmp.tiapi.customer.model.CounterPartyRole;
import com.tcmp.tiapi.customer.repository.CounterPartyRepository;
import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.InvoiceRepository;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.program.ProgramRepository;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResult;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResultMeta;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerService {
  private final CounterPartyRepository counterPartyRepository;
  private final InvoiceRepository invoiceRepository;
  private final ProgramRepository programRepository;

  private final InvoiceMapper invoiceMapper;

  public PaginatedResult<InvoiceDTO> getSellerInvoices(String sellerMnemonic, PageParams pageParams) {
    List<Long> sellerCounterpartyIds = counterPartyRepository.findUniqueIdsByCustomerMnemonicAndRole(
      sellerMnemonic, CounterPartyRole.SELLER.getValue());

    if (sellerCounterpartyIds.isEmpty()) {
      throw new NotFoundHttpException(
        String.format("Could not find invoices for seller with mnemonic %s.", sellerMnemonic));
    }

    Page<InvoiceMaster> invoiceMasterPage = invoiceRepository.findBySellerIdIn(
      sellerCounterpartyIds, PageRequest.of(pageParams.getPage(), pageParams.getSize()));

    Map<Long, CounterParty> idsToCounterParties = getIdsToCounterpartiesFromInvoicesPageAndSellerIds(invoiceMasterPage, sellerCounterpartyIds);
    Map<Long, Program> idsToPrograms = getIdsToProgramsFromInvoicesPage(invoiceMasterPage);

    List<InvoiceDTO> invoicesDTOs = invoiceMapper.mapEntitiesToDTOs(invoiceMasterPage.getContent(), idsToCounterParties, idsToPrograms);

    return PaginatedResult.<InvoiceDTO>builder()
      .data(invoicesDTOs)
      .meta(PaginatedResultMeta.builder()
        .isLastPage(invoiceMasterPage.isLast())
        .totalPages(invoiceMasterPage.getTotalPages())
        .totalItems(invoiceMasterPage.getTotalElements())
        .build())
      .build();
  }

  private Map<Long, CounterParty> getIdsToCounterpartiesFromInvoicesPageAndSellerIds(
    Page<InvoiceMaster> invoiceMasterPage,
    List<Long> sellerCounterpartyIds
  ) {
    List<Long> buyerCounterpartyIds = invoiceMasterPage.get()
      .map(InvoiceMaster::getBuyerId)
      .distinct()
      .toList();

    List<Long> uniqueCounterpartiesIds = Stream.concat(sellerCounterpartyIds.stream(), buyerCounterpartyIds.stream())
      .distinct()
      .toList();

    return counterPartyRepository.findByIdIn(uniqueCounterpartiesIds)
      .stream()
      .collect(Collectors.toMap(CounterParty::getId, counterParty -> counterParty));
  }

  private Map<Long, Program> getIdsToProgramsFromInvoicesPage(Page<InvoiceMaster> invoiceMasterPage) {
    List<Long> uniqueProgramIds = invoiceMasterPage.stream()
      .map(InvoiceMaster::getProgrammeId)
      .distinct()
      .toList();

    return programRepository.findByPkIn(uniqueProgramIds)
      .stream()
      .collect(Collectors.toMap(Program::getPk, program -> program));
  }
}
