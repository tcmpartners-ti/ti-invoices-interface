package com.tcmp.tiapi.customer.service;

import com.tcmp.tiapi.customer.dto.response.OutstandingBalanceDTO;
import com.tcmp.tiapi.customer.dto.response.SearchSellerInvoicesParams;
import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.repository.InvoiceSpecifications;
import com.tcmp.tiapi.program.ProgramMapper;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.program.repository.ProgramRepository;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResult;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResultMeta;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import com.tcmp.tiapi.shared.utils.MonetaryAmountUtils;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerService {
  private final CustomerRepository customerRepository;
  private final InvoiceRepository invoiceRepository;
  private final ProgramRepository programRepository;
  private final InvoiceMapper invoiceMapper;
  private final ProgramMapper programMapper;

  public PaginatedResult<InvoiceDTO> getSellerInvoices(
      String sellerMnemonic, SearchSellerInvoicesParams searchParams, PageParams pageParams) {
    if (!customerRepository.existsByIdMnemonic(sellerMnemonic)) {
      throw new NotFoundHttpException(
          String.format("Could not find a seller with mnemonic %s,", sellerMnemonic));
    }

    Page<InvoiceMaster> sellerInvoicesPage =
        invoiceRepository.findAll(
            InvoiceSpecifications.filterBySellerMnemonicAndStatus(
                sellerMnemonic, searchParams.status()),
            PageRequest.of(pageParams.getPage(), pageParams.getSize()));

    List<InvoiceDTO> invoicesDTOs =
        invoiceMapper.mapEntitiesToDTOs(sellerInvoicesPage.getContent());

    return PaginatedResult.<InvoiceDTO>builder()
        .data(invoicesDTOs)
        .meta(PaginatedResultMeta.from(sellerInvoicesPage))
        .build();
  }

  public PaginatedResult<ProgramDTO> getSellerProgramsByMnemonic(
      String sellerMnemonic, PageParams pageParams) {
    if (!customerRepository.existsByIdMnemonic(sellerMnemonic)) {
      throw new NotFoundHttpException(
          String.format("Could not find a seller with mnemonic %s.", sellerMnemonic));
    }

    PageRequest pageable = PageRequest.of(pageParams.getPage(), pageParams.getSize());
    Page<Program> programsPage =
        programRepository.findAllBySellerMnemonic(sellerMnemonic, pageable);

    return PaginatedResult.<ProgramDTO>builder()
        .data(programMapper.mapEntitiesToDTOs(programsPage.getContent()))
        .meta(PaginatedResultMeta.from(programsPage))
        .build();
  }

  public PaginatedResult<ProgramDTO> getSellerProgramsByCif(
      String sellerCif, PageParams pageParams) {
    if (!customerRepository.existsByNumber(sellerCif)) {
      throw new NotFoundHttpException(
          String.format("Could not find a seller with cif %s.", sellerCif));
    }

    PageRequest pageable = PageRequest.of(pageParams.getPage(), pageParams.getSize());
    Page<Program> programsPage = programRepository.findAllBySellerCif(sellerCif, pageable);

    return PaginatedResult.<ProgramDTO>builder()
        .data(programMapper.mapEntitiesToDTOs(programsPage.getContent()))
        .meta(PaginatedResultMeta.from(programsPage))
        .build();
  }

  public OutstandingBalanceDTO getSellerOutstandingBalanceByMnemonic(String sellerMnemonic) {
    if (!customerRepository.existsByIdMnemonic(sellerMnemonic)) {
      throw new NotFoundHttpException(
          String.format("Could not find a seller with mnemonic %s,", sellerMnemonic));
    }

    BigDecimal outstandingBalance =
        invoiceRepository
            .getOutstandingBalanceBySellerMnemonic(sellerMnemonic)
            .map(MonetaryAmountUtils::convertCentsToDollars)
            .orElse(BigDecimal.ZERO);

    return new OutstandingBalanceDTO(outstandingBalance);
  }
}
