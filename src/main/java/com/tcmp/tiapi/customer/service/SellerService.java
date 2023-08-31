package com.tcmp.tiapi.customer.service;

import com.tcmp.tiapi.customer.model.CounterParty;
import com.tcmp.tiapi.customer.model.CounterPartyRole;
import com.tcmp.tiapi.customer.repository.CounterPartyRepository;
import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.InvoiceRepository;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.dto.response.PaginatedResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerService {
  private final CounterPartyRepository counterPartyRepository;
  private final InvoiceRepository invoiceRepository;
  private final InvoiceMapper invoiceMapper;


  public PaginatedResult<InvoiceDTO> getSellerInvoices(String sellerMnemonic, PageParams pageParams) {
    List<Long> sellerCounterpartyIds = counterPartyRepository.findByCustomerMnemonicAndRole(sellerMnemonic, CounterPartyRole.SELLER.getValue())
      .stream()
      .map(CounterParty::getId)
      .toList();

    Page<InvoiceMaster> invoiceMasterPage = invoiceRepository.findBySellerIdIn(
      sellerCounterpartyIds, PageRequest.of(pageParams.getPage(), pageParams.getSize()));

    List<Long> buyerCounterpartyIds = invoiceMasterPage.get().map(InvoiceMaster::getBuyerId).toList();

    Set<Long> uniqueCounterPartiesIds = new HashSet<>();
    uniqueCounterPartiesIds.addAll(sellerCounterpartyIds);
    uniqueCounterPartiesIds.addAll(buyerCounterpartyIds);

    Map<Long, CounterParty> idToCounterParty = counterPartyRepository.findByIdIn(uniqueCounterPartiesIds.stream().toList())
      .stream().collect(Collectors.toMap(CounterParty::getId, counterParty -> counterParty));

    List<InvoiceDTO> pageData = invoiceMasterPage.stream().map(invoiceMaster -> invoiceMapper.mapEntityToDTO(
      invoiceMaster,
      idToCounterParty.getOrDefault(invoiceMaster.getBuyerId(), null),
      idToCounterParty.getOrDefault(invoiceMaster.getSellerId(), null),
      null
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

//  private List<>
}
