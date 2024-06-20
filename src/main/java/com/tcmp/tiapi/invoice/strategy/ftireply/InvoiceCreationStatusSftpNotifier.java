package com.tcmp.tiapi.invoice.strategy.ftireply;

import com.tcmp.tiapi.invoice.model.bulkcreate.BulkCreateInvoicesFileInfo;
import com.tcmp.tiapi.invoice.model.bulkcreate.InvoiceRowProcessingResult;
import com.tcmp.tiapi.invoice.repository.redis.BulkCreateInvoicesFileInfoRepository;
import com.tcmp.tiapi.invoice.repository.redis.InvoiceProcessingRowBulkRepository;
import com.tcmp.tiapi.invoice.repository.redis.InvoiceRowProcessingResultRepository;
import com.tcmp.tiapi.invoice.service.InvoiceFileHandler;
import com.tcmp.tiapi.invoice.service.InvoiceFullOutputFileService;
import com.tcmp.tiapi.invoice.service.InvoiceSummaryFileService;
import com.tcmp.tiapi.ti.dto.response.Details;
import com.tcmp.tiapi.ti.dto.response.ResponseStatus;
import com.tcmp.tiapi.ti.dto.response.ServiceResponse;
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
public class InvoiceCreationStatusSftpNotifier implements InvoiceCreationStatusNotifier {
  private final ProducerTemplate producerTemplate;

  private final InvoiceRowProcessingResultRepository invoiceRowProcessingResultRepository;
  private final InvoiceProcessingRowBulkRepository invoiceProcessingRowBulkRepository;
  private final BulkCreateInvoicesFileInfoRepository bulkCreateInvoicesFileInfoRepository;

  private final InvoiceFullOutputFileService invoiceFullOutputFileService;
  private final InvoiceSummaryFileService invoiceSummaryFileService;

  private final InvoiceFileHandler invoiceFileHandler;

  @Value("${fcm.route.sftp.from-full-output}")
  private String uriFromFullOutputFile;

  @Value("${fcm.route.sftp.from-summary}")
  private String uriFromSummaryFile;

  @Override
  public void notify(ServiceResponse serviceResponse) {
    String invoiceUuidFromCorrelationId = serviceResponse.getResponseHeader().getCorrelationId();
    String[] split = invoiceUuidFromCorrelationId.split(":");
    String fileUuid = split[0];
    Integer invoiceIndexInFile = Integer.parseInt(split[1]);

    saveProcessingResult(serviceResponse, fileUuid, invoiceIndexInFile);

    BulkCreateInvoicesFileInfo invoiceFileInfo =
        bulkCreateInvoicesFileInfoRepository
            .findById(fileUuid)
            .orElseThrow(() -> new EntityNotFoundException("Could not find file information."));

    String keyPattern = String.format("InvoiceRowProcessingResult:%s:*", fileUuid);
    long totalInvoicesProcessed =
        invoiceProcessingRowBulkRepository.totalRowsByIdPattern(keyPattern);
    boolean isLastInvoiceFromBatch = invoiceFileInfo.getTotalInvoices() == totalInvoicesProcessed;
    if (isLastInvoiceFromBatch) {
      String[] paths = generateFullOutputAndSummaryFiles(invoiceFileInfo, fileUuid);
      uploadFullOutputAndSummaryFiles(paths);
      cleanUpFullOutputAndSummaryFilesInformation(invoiceFileInfo, fileUuid, paths);
    }
  }

  private String[] generateFullOutputAndSummaryFiles(
      BulkCreateInvoicesFileInfo invoiceFileInfo, String fileUuid) {
    String fullOutputFilePath =
        invoiceFullOutputFileService.generateAndSaveFile(
            invoiceFileInfo.getOriginalFilename(), fileUuid);
    String summaryFilePath = invoiceSummaryFileService.generateAndSaveFile(invoiceFileInfo);

    log.info("Created full output file in: {}", fullOutputFilePath);
    log.info("Created summary file in: {}", summaryFilePath);

    return new String[] {fullOutputFilePath, summaryFilePath};
  }

  private void uploadFullOutputAndSummaryFiles(String... paths) {
    assert paths.length == 2;
    String fullOutputFilePath = paths[0];
    String summaryFilePath = paths[1];
    String fullOutputFilename = getFilenameFromPath(fullOutputFilePath);
    String summaryFilename = getFilenameFromPath(summaryFilePath);

    log.info("Uploading files.");
    producerTemplate.sendBodyAndHeaders(
        uriFromFullOutputFile, fullOutputFilePath, Map.of("CamelFileName", fullOutputFilename));
    producerTemplate.sendBodyAndHeaders(
        uriFromSummaryFile, summaryFilePath, Map.of("CamelFileName", summaryFilename));
  }

  private void cleanUpFullOutputAndSummaryFilesInformation(
      BulkCreateInvoicesFileInfo invoiceFileInfo, String fileUuid, String... paths) {
    assert paths.length == 2;

    invoiceFileHandler.deleteFile(paths[0]);
    invoiceFileHandler.deleteFile(paths[1]);
    bulkCreateInvoicesFileInfoRepository.deleteById(invoiceFileInfo.getId());
    invoiceRowProcessingResultRepository.deleteAllByFileUuid(fileUuid);
  }

  private void saveProcessingResult(
      ServiceResponse serviceResponse, String fileUuid, Integer invoiceIndexInFile) {
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
            .id(serviceResponse.getResponseHeader().getCorrelationId())
            .fileUuid(fileUuid)
            .index(invoiceIndexInFile)
            .status(status)
            .errorCodes(errorCodes)
            .build();
    invoiceRowProcessingResultRepository.save(invoiceProcessingResult);
  }

  private String getErrorCodeFromErrorMessage(String error) {
    String genericError = error.split(" - ")[0];

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
