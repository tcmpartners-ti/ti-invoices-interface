package com.tcmp.tiapi.invoice.strategy.ftireply;

import com.tcmp.tiapi.invoice.model.InvoiceEventInfo;
import com.tcmp.tiapi.invoice.model.bulkcreate.BulkCreateInvoicesFileInfo;
import com.tcmp.tiapi.invoice.model.bulkcreate.InvoiceRowProcessingResult;
import com.tcmp.tiapi.invoice.repository.redis.BulkCreateInvoicesFileInfoRepository;
import com.tcmp.tiapi.invoice.repository.redis.InvoiceProcessingRowBulkRepository;
import com.tcmp.tiapi.invoice.repository.redis.InvoiceRowProcessingResultRepository;
import com.tcmp.tiapi.invoice.service.InvoiceEventService;
import com.tcmp.tiapi.invoice.service.InvoiceFileHandler;
import com.tcmp.tiapi.invoice.service.InvoiceFullOutputFileService;
import com.tcmp.tiapi.invoice.service.InvoiceSummaryFileService;
import com.tcmp.tiapi.ti.dto.response.Details;
import com.tcmp.tiapi.ti.dto.response.ResponseStatus;
import com.tcmp.tiapi.ti.dto.response.ServiceResponse;
import com.tcmp.tiapi.ti.route.fti.FTIReplyIncomingStrategy;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingMapper;
import com.tcmp.tiapi.titoapigee.businessbanking.BusinessBankingService;
import com.tcmp.tiapi.titoapigee.businessbanking.dto.request.OperationalGatewayRequestPayload;
import com.tcmp.tiapi.titoapigee.businessbanking.model.OperationalGatewayProcessCode;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceCreationStatusNotifierStrategy implements FTIReplyIncomingStrategy {
  private final ProducerTemplate producerTemplate;

  private final BulkCreateInvoicesFileInfoRepository bulkCreateInvoicesFileInfoRepository;
  private final InvoiceRowProcessingResultRepository invoiceRowProcessingResultRepository;
  private final InvoiceProcessingRowBulkRepository invoiceProcessingRowBulkRepository;
  private final InvoiceEventService invoiceEventService;
  private final InvoiceFullOutputFileService invoiceFullOutputFileService;
  private final InvoiceSummaryFileService invoiceSummaryFileService;
  private final BusinessBankingService businessBankingService;
  private final BusinessBankingMapper businessBankingMapper;
  private final InvoiceFileHandler invoiceFileHandler;

  @Value("${fcm.route.sftp.from-full-output}")
  private String uriFromFullOutputFile;

  @Value("${fcm.route.sftp.from-summary}")
  private String uriFromSummaryFile;

  @Override
  public void handleServiceResponse(ServiceResponse serviceResponse) {
    String invoiceUuidFromCorrelationId = serviceResponse.getResponseHeader().getCorrelationId();

    boolean isSftpChannelCorrelationUuid = invoiceUuidFromCorrelationId.split(":").length == 2;
    if (isSftpChannelCorrelationUuid) {
      notifyToSftpChannel(serviceResponse, invoiceUuidFromCorrelationId);
      return;
    }

    notifyToBusinessBankingChannel(serviceResponse, invoiceUuidFromCorrelationId);
  }

  private void notifyToBusinessBankingChannel(
      ServiceResponse serviceResponse, String invoiceUuidFromCorrelationId) {
    String responseStatus = serviceResponse.getResponseHeader().getStatus();
    boolean creationWasSuccessful = ResponseStatus.SUCCESS.getValue().equals(responseStatus);
    if (creationWasSuccessful) {
      log.info("Invoice created successfully, don't notify.");
      invoiceEventService.deleteInvoiceByUuid(invoiceUuidFromCorrelationId);
      return;
    }

    try {
      InvoiceEventInfo invoice =
          invoiceEventService.findInvoiceEventInfoByUuid(invoiceUuidFromCorrelationId);

      OperationalGatewayRequestPayload payload =
          businessBankingMapper.mapToRequestPayload(serviceResponse, invoice);

      businessBankingService.notifyEvent(OperationalGatewayProcessCode.INVOICE_CREATED, payload);
      invoiceEventService.deleteInvoiceByUuid(invoiceUuidFromCorrelationId);
    } catch (EntityNotFoundException e) {
      log.error(e.getMessage());
    }
  }

  private void notifyToSftpChannel(
      ServiceResponse serviceResponse, String invoiceUuidFromCorrelationId) {
    String[] split = invoiceUuidFromCorrelationId.split(":");
    String fileUuid = split[0];
    Integer invoiceIndexInFile = Integer.parseInt(split[1]);

    boolean isSuccessfulResponse =
        ResponseStatus.SUCCESS.getValue().equals(serviceResponse.getResponseHeader().getStatus());
    InvoiceRowProcessingResult.Status status =
        isSuccessfulResponse
            ? InvoiceRowProcessingResult.Status.PENDING
            : InvoiceRowProcessingResult.Status.NOT_PROCESSED;
    Details details = serviceResponse.getResponseHeader().getDetails();
    List<String> errorCodes =
        details != null && details.getErrors() != null
            ? details.getErrors().stream().map(this::getErrorCodeFromErrorMessage).toList()
            : List.of();

    InvoiceRowProcessingResult invoiceProcessingResult =
        InvoiceRowProcessingResult.builder()
            .id(invoiceUuidFromCorrelationId)
            .fileUuid(fileUuid)
            .index(invoiceIndexInFile)
            .status(status)
            .errorCodes(errorCodes)
            .build();
    invoiceRowProcessingResultRepository.save(invoiceProcessingResult);

    BulkCreateInvoicesFileInfo invoiceFileInfo =
        bulkCreateInvoicesFileInfoRepository
            .findById(fileUuid)
            .orElseThrow(() -> new EntityNotFoundException("Could not find file information."));

    String keyPattern = "InvoiceRowProcessingResult:" + fileUuid + ":*";
    long totalInvoicesProcessed =
        invoiceProcessingRowBulkRepository.totalRowsByIdPattern(keyPattern);
    boolean isLastInvoiceFromBatch = invoiceFileInfo.getTotalInvoices() == totalInvoicesProcessed;
    if (isLastInvoiceFromBatch) {
      String fullOutputFilePath =
          invoiceFullOutputFileService.generateAndSaveFile(
              invoiceFileInfo.getOriginalFilename(), fileUuid);
      String summaryFilePath = invoiceSummaryFileService.generateAndSaveFile(invoiceFileInfo);

      log.info("Created full output file in: {}", fullOutputFilePath);
      log.info("Created summary file in: {}", summaryFilePath);

      log.info("Uploading files.");
      producerTemplate.sendBodyAndHeaders(
          uriFromFullOutputFile,
          fullOutputFilePath,
          Map.of("CamelFileName", getFilenameFromPath(fullOutputFilePath)));
      producerTemplate.sendBodyAndHeaders(
          uriFromSummaryFile,
          summaryFilePath,
          Map.of("CamelFileName", getFilenameFromPath(summaryFilePath)));

      invoiceFileHandler.deleteFile(fullOutputFilePath);
      invoiceFileHandler.deleteFile(summaryFilePath);

      // Cleanup all information
      bulkCreateInvoicesFileInfoRepository.deleteById(invoiceFileInfo.getId());
      invoiceRowProcessingResultRepository.deleteAllByFileUuid(fileUuid);
    }
  }

  private String getErrorCodeFromErrorMessage(String error) {
    var genericError = error.split(" - ")[0];

    return switch (genericError) {
      case "Duplicate invoice number" -> "001";
      case "Invalid programme / seller / buyer relationship" -> "002";
      case "Invalid outstanding amount" -> "003";
      case "Invalid programme" -> "004";
      case "Event aborted Create Invoice in master" -> "005";
      case "Invalid anchor party" -> "006";
      case "Invalid seller" -> "007";
      case "Invalid buyer" -> "008";
      case "Invalid invoice number" -> "009";
      case "Invalid issue date" -> "010";
      case "Invalid settlement date" -> "011";
      case "Invalid Face Value" -> "012";
      case "Issue date is later than settlement date" -> "014";
      case "There is a dependency" -> "016";
      case "Invoice amount exceeds buyer limit availability (BuyerExpos)" -> "017";
      default -> "000";
    };
  }

  private String getFilenameFromPath(String filePath) {
    if (filePath == null || filePath.isEmpty()) return "";

    int lastSeparatorIndex = filePath.lastIndexOf("/");
    if (lastSeparatorIndex == -1) {
      return filePath;
    }

    return filePath.substring(lastSeparatorIndex + 1);
  }
}
