package com.tcmp.tiapi.invoice.service;

import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.dto.request.InvoiceCreationDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceFinancingDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceSearchParams;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.repository.redis.InvoiceEventRepository;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import com.tcmp.tiapi.ti.TIServiceRequestWrapper;
import com.tcmp.tiapi.ti.dto.TIOperation;
import com.tcmp.tiapi.ti.dto.TIService;
import com.tcmp.tiapi.ti.dto.request.ReplyFormat;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {
  private final ProducerTemplate producerTemplate;

  private final InvoiceRepository invoiceRepository;
  private final InvoiceEventRepository invoiceEventRepository;
  private final InvoiceMapper invoiceMapper;

  private final TIServiceRequestWrapper serviceRequestWrapper;

  @Value("${ti.route.fti.out.from}")
  private String uriFromFtiOutgoing;

  public InvoiceDTO getInvoiceById(Long invoiceId) {
    InvoiceMaster invoice =
        invoiceRepository
            .findById(invoiceId)
            .orElseThrow(
                () ->
                    new NotFoundHttpException(
                        String.format("Could not find invoice with id %s.", invoiceId)));

    return invoiceMapper.mapEntityToDTO(invoice);
  }

  public InvoiceDTO searchInvoice(InvoiceSearchParams searchParams) {
    InvoiceMaster invoice =
        invoiceRepository
            .findByProgramIdAndSellerMnemonicAndReferenceAndProductMasterIsActive(
                searchParams.programme(), searchParams.seller(), searchParams.invoice(), true)
            .orElseThrow(
                () ->
                    new NotFoundHttpException(
                        String.format(
                            "Could not find an active invoice %s for the given program and seller.",
                            searchParams.invoice())));

    return invoiceMapper.mapEntityToDTO(invoice);
  }

  public void createSingleInvoiceInTi(InvoiceCreationDTO creationDTO) {
    String invoiceUuid = UUID.randomUUID().toString();

    invoiceEventRepository.save(
        InvoiceEventInfo.builder()
            .id(invoiceUuid)
            .batchId(null)
            .reference(creationDTO.getInvoiceNumber())
            .sellerMnemonic(creationDTO.getSeller())
            .build());

    producerTemplate.sendBody(
        uriFromFtiOutgoing,
        serviceRequestWrapper.wrapRequest(
            TIService.TRADE_INNOVATION,
            TIOperation.CREATE_INVOICE,
            ReplyFormat.STATUS,
            invoiceUuid,
            invoiceMapper.mapDTOToFTIMessage(creationDTO)));
  }

  public void financeInvoice(InvoiceFinancingDTO financingDTO) {
    String invoiceUuid = UUID.randomUUID().toString();

    InvoiceMaster invoice = findInvoiceFromFinancingInfo(financingDTO);
    invoiceEventRepository.save(
        InvoiceEventInfo.builder()
            .id(invoiceUuid)
            .batchId(invoice.getBatchId().trim())
            .reference(financingDTO.getInvoice().getNumber())
            .sellerMnemonic(financingDTO.getSeller())
            .build());

    producerTemplate.sendBody(
        uriFromFtiOutgoing,
        serviceRequestWrapper.wrapRequest(
            TIService.TRADE_INNOVATION,
            TIOperation.FINANCE_INVOICE,
            ReplyFormat.STATUS,
            invoiceUuid,
            invoiceMapper.mapFinancingDTOToFTIMessage(financingDTO)));
  }

  /**
   * This method is required to get the `batchId` for the financing process.
   *
   * @param financingDTO Financing request information.
   * @return InvoiceMaster if found.
   */
  private InvoiceMaster findInvoiceFromFinancingInfo(InvoiceFinancingDTO financingDTO) {
    return invoiceRepository
        .findByProgramIdAndSellerMnemonicAndReference(
            financingDTO.getProgramme(),
            financingDTO.getSeller(),
            financingDTO.getInvoice().getNumber())
        .orElseThrow(
            () ->
                new NotFoundHttpException(
                    "Could not find invoice for given programme / buyer / seller relationship."));
  }
}
