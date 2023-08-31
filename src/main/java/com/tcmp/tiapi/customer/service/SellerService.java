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
import com.tcmp.tiapi.shared.dto.response.PaginatedResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerService {
  private final CounterPartyRepository counterPartyRepository;
  private final InvoiceRepository invoiceRepository;
  private final ProgramRepository programRepository;

  private final InvoiceMapper invoiceMapper;

  public PaginatedResult<InvoiceDTO> getSellerInvoices(String sellerMnemonic, PageParams pageParams) {
    List<Long> sellerCounterpartyIds = counterPartyRepository.findByCustomerMnemonicAndRole(sellerMnemonic, CounterPartyRole.SELLER.getValue())
      .stream()
      .map(CounterParty::getId)
      .toList();

    Page<InvoiceMaster> invoiceMasterPage = invoiceRepository.findBySellerIdIn(
      sellerCounterpartyIds, PageRequest.of(pageParams.getPage(), pageParams.getSize()));

    List<Long> buyerCounterpartyIds = invoiceMasterPage.get().map(InvoiceMaster::getBuyerId).toList();

    List<Long> uniqueCounterPartiesIds = getUniqueCounterPartiesIds(buyerCounterpartyIds, sellerCounterpartyIds);

    Map<Long, CounterParty> idToCounterParty = counterPartyRepository.findByIdIn(uniqueCounterPartiesIds)
      .stream()
      .collect(Collectors.toMap(CounterParty::getId, counterParty -> counterParty));

    List<Long> uniqueProgramIds = invoiceMasterPage.stream()
      .map(InvoiceMaster::getProgrammeId)
      .distinct()
      .toList();
    Map<Long, Program> idToProgram = programRepository.findByPkIn(uniqueProgramIds)
      .stream().collect(Collectors.toMap(Program::getPk, program -> program));

    List<InvoiceDTO> pageData = invoiceMasterPage.stream().map(invoiceMaster -> invoiceMapper.mapEntityToDTO(
      invoiceMaster,
      idToCounterParty.getOrDefault(invoiceMaster.getBuyerId(), null),
      idToCounterParty.getOrDefault(invoiceMaster.getSellerId(), null),
      idToProgram.getOrDefault(invoiceMaster.getProgrammeId(), null)
    )).toList();

    return PaginatedResult.<InvoiceDTO>builder()
      .data(pageData)
      .meta(Map.of(
        "pagination", Map.of(
          "isLastPage", invoiceMasterPage.isLast(),
          "totalPages", invoiceMasterPage.getTotalPages()
        )
      ))
      .build();
  }

  private List<Long> getUniqueCounterPartiesIds(List<Long> buyerCounterpartyIds, List<Long> sellerCounterpartyIds) {
    List<Long> mergedIds = new ArrayList<>(buyerCounterpartyIds);
    mergedIds.addAll(sellerCounterpartyIds);

    return mergedIds.stream().distinct().toList();
  }
}
