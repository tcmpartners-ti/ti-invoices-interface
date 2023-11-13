package com.tcmp.tiapi.invoice.service;

import com.tcmp.tiapi.invoice.InvoiceConfiguration;
import com.tcmp.tiapi.invoice.InvoiceMapper;
import com.tcmp.tiapi.invoice.dto.request.InvoiceCreationDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceFinancingDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceSearchParams;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.invoice.dto.ti.creation.CreateInvoiceEventMessage;
import com.tcmp.tiapi.invoice.dto.ti.finance.FinanceBuyerCentricInvoiceEventMessage;
import com.tcmp.tiapi.invoice.exception.FieldsInconsistenciesException;
import com.tcmp.tiapi.invoice.model.InvoiceMaster;
import com.tcmp.tiapi.invoice.repository.InvoiceRepository;
import com.tcmp.tiapi.shared.dto.response.CurrencyAmountDTO;
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
import java.util.List;
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
    InvoiceMaster invoice = invoiceRepository.findByProgramIdAndSellerMnemonicAndReferenceAndProductMasterIsActive(
        searchParams.programme(),
        searchParams.seller(),
        searchParams.invoice(),
        true
      )
      .orElseThrow(() -> new NotFoundHttpException(
        String.format("Could not find an active invoice %s for the given program and seller.", searchParams.invoice())));

    return invoiceMapper.mapEntityToDTO(invoice);
  }

  public void createSingleInvoiceInTi(InvoiceCreationDTO invoiceDTO) {
    if (!areBuyerMnemonicFieldsEquals(invoiceDTO)) {
      throw new FieldsInconsistenciesException("The 'buyer' fields do not match.", List.of(
        "context.customer",
        "anchorParty",
        "buyer"
      ));
    }

    if (!areInvoiceReferenceFieldsEquals(invoiceDTO)) {
      throw new FieldsInconsistenciesException("The 'invoice number' fields do not match.", List.of(
        "context.theirReference",
        "invoiceNumber"
      ));
    }

    if (!areInvoiceMonetaryFieldsEquals(invoiceDTO)) {
      throw new FieldsInconsistenciesException("The 'amount and currency' fields do not match.", List.of(
        "faceValue",
        "outstandingAmount"
      ));
    }

    CreateInvoiceEventMessage createInvoiceEventMessage = invoiceMapper.mapDTOToFTIMessage(invoiceDTO);

    producerTemplate.sendBody(invoiceConfiguration.getUriCreateFrom(), createInvoiceEventMessage);
  }

  private boolean areBuyerMnemonicFieldsEquals(InvoiceCreationDTO invoiceDTO) {
    String customer = invoiceDTO.getContext().getCustomer();
    String anchorParty = invoiceDTO.getAnchorParty();
    String buyerMnemonic = invoiceDTO.getBuyer();

    return customer.equals(anchorParty) && anchorParty.equals(buyerMnemonic);
  }

  private boolean areInvoiceReferenceFieldsEquals(InvoiceCreationDTO invoiceDTO) {
    String theirReference = invoiceDTO.getContext().getTheirReference();
    String invoiceNumber = invoiceDTO.getInvoiceNumber();

    return theirReference.equals(invoiceNumber);
  }

  private boolean areInvoiceMonetaryFieldsEquals(InvoiceCreationDTO invoiceDTO) {
    CurrencyAmountDTO faceValue = invoiceDTO.getFaceValue();
    CurrencyAmountDTO outstandingValue = invoiceDTO.getOutstandingAmount();

    return faceValue.getAmount().compareTo(outstandingValue.getAmount()) == 0
           && faceValue.getCurrency().equals(outstandingValue.getCurrency());
  }

  public void createMultipleInvoicesInTi(MultipartFile invoicesFile, String batchId) {
    if (invoicesFile.isEmpty()) throw new InvalidFileHttpException("File is empty.");

    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(invoicesFile.getInputStream()))) {
      log.info("Sending invoices to TI.");

      producerTemplate.sendBodyAndHeaders(
        invoiceConfiguration.getUriBulkCreateFrom(),
        bufferedReader,
        Map.ofEntries(
          Map.entry("batchId", batchId)
        )
      );
    } catch (IOException e) {
      log.error("Invalid file uploaded");
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
