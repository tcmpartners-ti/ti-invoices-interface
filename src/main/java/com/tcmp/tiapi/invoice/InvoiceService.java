package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.invoice.messaging.CreateInvoiceEventMessage;
import com.tcmp.tiapi.messaging.TIServiceRequestWrapper;
import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.messaging.model.TIService;
import com.tcmp.tiapi.messaging.model.requests.ServiceRequest;
import com.tcmp.tiapi.shared.exception.InvalidFileHttpException;
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

  public String sendAndReceiveInvoiceUUID(CreateInvoiceEventMessage createInvoiceEventMessage) {
    ServiceRequest<CreateInvoiceEventMessage> createInvoiceEventMessageServiceRequest =
      TIServiceRequestWrapper.wrapRequest(
        TIService.TRADE_INNOVATION, TIOperation.CREATE_INVOICE, createInvoiceEventMessage);

    log.info("Invoice sent to be created.");

    return producerTemplate.requestBodyAndHeaders(
      InvoiceRouter.DIRECT_CREATE_INVOICE,
      createInvoiceEventMessageServiceRequest,
      Map.of(
        "TIService", TIService.TRADE_INNOVATION_VALUE,
        "TIOperation", TIOperation.CREATE_INVOICE_VALUE
      ),
      String.class
    );
  }

  // Todo: Improve this method
  public void createMultipleInvoices(MultipartFile invoicesFile) {
    if (invoicesFile.isEmpty()) throw new InvalidFileHttpException("File is empty.");

    try {
      InputStreamReader inputStreamReader = new InputStreamReader(invoicesFile.getInputStream());

      try (BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
        producerTemplate.sendBody(InvoiceRouter.DIRECT_CREATE_INVOICES_BULK, bufferedReader);
      }
    } catch (IOException e) {
      throw new InvalidFileHttpException("Could not read the uploaded file");
    }
  }
}
