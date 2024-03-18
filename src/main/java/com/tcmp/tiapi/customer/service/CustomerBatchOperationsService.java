package com.tcmp.tiapi.customer.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import com.tcmp.tiapi.customer.dto.csv.CustomerCreationCSVRow;
import com.tcmp.tiapi.customer.dto.ti.CustomerItemRequest;
import com.tcmp.tiapi.customer.mapper.CustomerMapper;
import com.tcmp.tiapi.shared.exception.CsvValidationException;
import com.tcmp.tiapi.shared.exception.InvalidFileHttpException;
import com.tcmp.tiapi.ti.dto.MaintenanceType;
import com.tcmp.tiapi.ti.dto.request.ServiceRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerBatchOperationsService {
  private final ProducerTemplate producerTemplate;
  private final CustomerMapper customerMapper;

  @Value("${ti.route.fti.out.from}")
  private String uriFtiOutgoingFrom;

  public void createMultipleCustomersInTi(MultipartFile customersFile) {
    if (customersFile.isEmpty()) throw new InvalidFileHttpException("File is empty.");

    List<CustomerCreationCSVRow> customers = getCustomerCreationCSVRows(customersFile);
    validateCustomerBeans(customers);

    log.info("Creating multiple customers in TI");
    processCustomersRows(MaintenanceType.DEFINE, customers);
  }

  public void deleteMultipleCustomersInTi(MultipartFile customersFile) {
    if (customersFile.isEmpty()) throw new InvalidFileHttpException("File is empty.");

    List<CustomerCreationCSVRow> customers = getCustomerCreationCSVRows(customersFile);
    validateCustomerBeans(customers);

    log.info("Deleting multiple customers in TI");
    processCustomersRows(MaintenanceType.DELETE, customers);
  }

  private static List<CustomerCreationCSVRow> getCustomerCreationCSVRows(
      MultipartFile customersFile) {
    try (BufferedReader bufferedReader =
        new BufferedReader(new InputStreamReader(customersFile.getInputStream()))) {
      CsvToBean<CustomerCreationCSVRow> customerRows =
          new CsvToBeanBuilder<CustomerCreationCSVRow>(bufferedReader)
              .withType(CustomerCreationCSVRow.class)
              .withIgnoreEmptyLine(true)
              .build();

      // Is this storing all rows in memory? If so, it could be a problem with large files
      return customerRows.parse();
    } catch (IOException e) {
      throw new InvalidFileHttpException("Could not process file");
    }
  }

  private void validateCustomerBeans(List<CustomerCreationCSVRow> customerRows)
      throws CsvValidationException {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();

    List<String> fileErrors = new ArrayList<>();
    int rowNumber = 1;

    for (CustomerCreationCSVRow customerRow : customerRows) {
      Set<ConstraintViolation<CustomerCreationCSVRow>> violations = validator.validate(customerRow);

      if (!violations.isEmpty()) {
        int line = rowNumber; // This is done to consume the row number in the lambda
        List<String> errors =
            violations.stream()
                .map(v -> String.format("[Row #%d] %s", line, v.getMessage()))
                .toList();
        fileErrors.addAll(errors);
      }

      rowNumber++;
    }

    if (!fileErrors.isEmpty()) {
      throw new CsvValidationException("Customer file has inconsistencies", fileErrors);
    }
  }

  private void processCustomersRows(
      MaintenanceType maintenanceType, List<CustomerCreationCSVRow> customerRows) {
    for (CustomerCreationCSVRow customerRow : customerRows) {
      ServiceRequest<CustomerItemRequest> itemsServiceRequest =
          customerMapper.mapCustomerAndAccountToBulkRequest(maintenanceType, customerRow);

      producerTemplate.asyncSendBody(uriFtiOutgoingFrom, itemsServiceRequest);
    }
  }
}
