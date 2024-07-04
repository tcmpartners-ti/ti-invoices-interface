package com.tcmp.tiapi.invoice.strategy.ftireply;

import com.tcmp.tiapi.invoice.model.bulkcreate.BulkCreateInvoicesFileInfo;
import com.tcmp.tiapi.invoice.model.bulkcreate.InvoiceRowProcessingResult;
import com.tcmp.tiapi.invoice.repository.redis.BulkCreateInvoicesFileInfoRepository;
import com.tcmp.tiapi.invoice.repository.redis.InvoiceProcessingRowBulkRepository;
import com.tcmp.tiapi.invoice.repository.redis.InvoiceRowProcessingResultRepository;
import com.tcmp.tiapi.invoice.service.files.InvoiceFileHandler;
import com.tcmp.tiapi.invoice.service.files.InvoiceLocalFileUploader;
import com.tcmp.tiapi.invoice.service.files.fulloutput.InvoiceFullOutputFileBuilder;
import com.tcmp.tiapi.invoice.service.files.realoutput.InvoiceRealOutputFileUploader;
import com.tcmp.tiapi.invoice.service.files.summary.InvoiceSummaryFileBuilder;
import com.tcmp.tiapi.ti.dto.response.Details;
import com.tcmp.tiapi.ti.dto.response.ResponseStatus;
import com.tcmp.tiapi.ti.dto.response.ServiceResponse;
import com.tcmp.tiapi.titofcm.config.FcmAzureContainerConfiguration;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InvoiceCreationStatusSftpNotifier implements InvoiceCreationStatusNotifier {
  private static final String PATH_DELIMITER = "/";

  private final FcmAzureContainerConfiguration containerConfiguration;
  private final BulkCreateInvoicesFileInfoRepository bulkCreateInvoicesFileInfoRepository;
  private final InvoiceProcessingRowBulkRepository invoiceProcessingRowBulkRepository;
  private final InvoiceRowProcessingResultRepository invoiceRowProcessingResultRepository;

  private final InvoiceFileHandler invoiceFileHandler;
  private final InvoiceFullOutputFileBuilder invoiceFullOutputFileBuilder;
  private final InvoiceLocalFileUploader invoiceLocalFileUploader;
  private final InvoiceRealOutputFileUploader invoiceRealOutputFileUploader;
  private final InvoiceSummaryFileBuilder invoiceSummaryFileBuilder;

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
      processAndUploadFullOutputFile(invoiceFileInfo, fileUuid);
      processAndUploadSummaryFile(invoiceFileInfo);

      invoiceRowProcessingResultRepository.deleteAllByFileUuid(fileUuid);

      invoiceRealOutputFileUploader.createHeader(invoiceFileInfo.getOriginalFilename());
    }
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

  private void processAndUploadFullOutputFile(
      BulkCreateInvoicesFileInfo fileInfo, String fileUuid) {
    List<InvoiceRowProcessingResult> invoiceProcessingResults =
        invoiceRowProcessingResultRepository.findAllByFileUuidOrderByIndex(fileUuid);
    String localPath =
        invoiceFullOutputFileBuilder.generateAndSaveFile(
            fileInfo.getOriginalFilename(), invoiceProcessingResults);

    String filename = getFilenameFromPath(localPath);
    String remotePath =
        containerConfiguration.remoteDirectories().fullOutput() + PATH_DELIMITER + filename;

    invoiceLocalFileUploader.uploadFromPath(localPath, remotePath);
    invoiceFileHandler.deleteFile(localPath);

    log.info("Uploaded full output file to {}.", remotePath);
  }

  private void processAndUploadSummaryFile(BulkCreateInvoicesFileInfo fileInfo) {
    long totalInvoicesSucceeded =
        invoiceRowProcessingResultRepository
            .findAllByFileUuidAndStatus(fileInfo.getId(), InvoiceRowProcessingResult.Status.PENDING)
            .size();

    String localPath =
        invoiceSummaryFileBuilder.generateAndSaveFile(fileInfo, totalInvoicesSucceeded);
    String filename = getFilenameFromPath(localPath);
    String remotePath =
        containerConfiguration.remoteDirectories().summary() + PATH_DELIMITER + filename;

    invoiceLocalFileUploader.uploadFromPath(localPath, remotePath);
    invoiceFileHandler.deleteFile(localPath);

    log.info("Uploaded summary file to {}.", remotePath);
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
