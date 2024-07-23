package com.tcmp.tiapi.invoice.service;

import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.dto.request.InvoiceCreationDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceFinancingDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceSearchParams;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.invoice.dto.ti.creation.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.invoice.repository.redis.InvoiceEventRepository;
import com.tcmp.tiapi.program.model.InterestTier;
import com.tcmp.tiapi.program.repository.InterestTierRepository;
import com.tcmp.tiapi.shared.UUIDGenerator;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import com.tcmp.tiapi.ti.TIServiceRequestWrapper;
import com.tcmp.tiapi.ti.dto.TIOperation;
import com.tcmp.tiapi.ti.dto.TIService;
import com.tcmp.tiapi.ti.dto.request.ReplyFormat;
import com.tcmp.tiapi.ti.dto.request.ServiceRequest;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {
  private final InterestTierRepository interestTierRepository;
  private final ProducerTemplate producerTemplate;

  private final InvoiceRepository invoiceRepository;
  private final InvoiceEventRepository invoiceEventRepository;
  private final InvoiceMapper invoiceMapper;

  private final TIServiceRequestWrapper serviceRequestWrapper;
  private final UUIDGenerator uuidGenerator;

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

    BigDecimal programInterestRate =
        interestTierRepository
            .findByProgrammeIdAndSellerId(invoice.getProgrammeId(), invoice.getSellerId())
            .map(InterestTier::getRate)
            .orElseGet(
                () ->
                    interestTierRepository
                        .findByProgrammeId(invoice.getProgrammeId())
                        .map(InterestTier::getRate)
                        .orElse(BigDecimal.ZERO));

    return invoiceMapper.mapEntityToDTO(invoice, programInterestRate);
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

    BigDecimal programInterestRate =
        interestTierRepository
            .findByProgrammeIdAndSellerId(invoice.getProgrammeId(), invoice.getSellerId())
            .map(InterestTier::getRate)
            .orElse(BigDecimal.ZERO);

    return invoiceMapper.mapEntityToDTO(invoice, programInterestRate);
  }

  public void createSingleInvoiceInTi(InvoiceCreationDTO creationDTO) {
    String invoiceUuid = uuidGenerator.getNewId();

    invoiceEventRepository.save(
        InvoiceEventInfo.builder()
            .id(invoiceUuid)
            .batchId(null)
            .reference(creationDTO.getInvoiceNumber())
            .sellerMnemonic(creationDTO.getSeller())
            .build());

    ServiceRequest<CreateInvoiceEventMessage> createInvoiceTiRequest =
        serviceRequestWrapper.wrapRequest(
            TIService.TRADE_INNOVATION,
            TIOperation.CREATE_INVOICE,
            ReplyFormat.STATUS,
            invoiceUuid,
            invoiceMapper.mapDTOToFTIMessage(creationDTO));

    producerTemplate.sendBody(uriFromFtiOutgoing, createInvoiceTiRequest);
  }

  public void financeInvoice(InvoiceFinancingDTO financingDTO) {
    String invoiceUuid = uuidGenerator.getNewId();

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
