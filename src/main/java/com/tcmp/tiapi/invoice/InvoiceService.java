package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.invoice.dto.request.InvoiceCreationDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceFinancingDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceSearchParams;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.invoice.dto.ti.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.dto.ti.FinanceBuyerCentricInvoiceEventMessage;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.shared.exception.InvalidFileHttpException;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceService {
  private final ProducerTemplate producerTemplate;

  private final InvoiceConfiguration invoiceConfiguration;
  private final InvoiceRepository invoiceRepository;
  private final InvoiceMapper invoiceMapper;

  public InvoiceDTO getInvoiceById(Long invoiceId) {
    InvoiceMaster invoice = invoiceRepository.findById(invoiceId)
      .orElseThrow(() -> new NotFoundHttpException(
        String.format("Could not find invoice with id %s.", invoiceId)));

    return invoiceMapper.mapEntityToDTO(invoice);
  }

  public InvoiceDTO searchInvoice(InvoiceSearchParams searchParams) {
    InvoiceMaster invoice = invoiceRepository.findByProgramIdAndSellerMnemonicAndReference(
        searchParams.programme(),
        searchParams.seller(),
        searchParams.invoice()
      )
      .orElseThrow(() -> new NotFoundHttpException(
        String.format("Could not find the invoice %s for the given program and seller.", searchParams.invoice())));

    return invoiceMapper.mapEntityToDTO(invoice);
  }

  public void createSingleInvoiceInTi(InvoiceCreationDTO invoiceDTO) {
    CreateInvoiceEventMessage createInvoiceEventMessage = invoiceMapper.mapDTOToFTIMessage(invoiceDTO);

    log.info("[Invoice: Create] {}", createInvoiceEventMessage);

    producerTemplate.sendBodyAndHeaders(
      invoiceConfiguration.getUriCreateFrom(),
      createInvoiceEventMessage,
      Map.ofEntries(
        Map.entry("JMSCorrelationID", createInvoiceEventMessage.getInvoiceNumber())
      )
    );
  }

  public void createMultipleInvoicesInTi(MultipartFile invoicesFile, String batchId) {
    if (invoicesFile.isEmpty()) throw new InvalidFileHttpException("File is empty.");

    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(invoicesFile.getInputStream()))) {
      log.info("[Invoice: bulk create] Sending invoices to TI.");

      producerTemplate.sendBodyAndHeaders(
        invoiceConfiguration.getUriBulkCreateFrom(),
        bufferedReader,
        Map.ofEntries(
          Map.entry("batchId", batchId)
        )
      );
    } catch (IOException e) {
      log.error("[Invoice: bulk create] Invalid file uploaded");
      throw new InvalidFileHttpException("Could not read the uploaded file");
    }
  }

  public void financeInvoice(InvoiceFinancingDTO invoiceFinancingDTO) {
    FinanceBuyerCentricInvoiceEventMessage financeInvoiceMessage = invoiceMapper.mapFinancingDTOToFTIMessage(invoiceFinancingDTO);

    producerTemplate.sendBody(
      invoiceConfiguration.getUriFinanceFrom(),
      financeInvoiceMessage
    );
  }
}
