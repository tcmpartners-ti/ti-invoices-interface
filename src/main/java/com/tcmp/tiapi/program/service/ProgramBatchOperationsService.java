package com.tcmp.tiapi.program.service;

import com.opencsv.bean.CsvToBeanBuilder;
import com.tcmp.tiapi.program.dto.csv.BaseRateCsvRow;
import com.tcmp.tiapi.program.dto.csv.ProgramCreationCsvRow;
import com.tcmp.tiapi.program.mapper.BaseRateMapper;
import com.tcmp.tiapi.program.mapper.ProgramMapper;
import com.tcmp.tiapi.shared.exception.CsvValidationException;
import com.tcmp.tiapi.shared.exception.InvalidFileHttpException;
import com.tcmp.tiapi.ti.dto.MaintenanceType;
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
public class ProgramBatchOperationsService {
  private final ProducerTemplate producerTemplate;
  private final ProgramMapper programMapper;
  private final BaseRateMapper baseRateMapper;

  @Value("${ti.route.fti.out.from}")
  private String uriFtiOutgoingFrom;

  public void createMultipleProgramsInTi(MultipartFile programsFile) {
    if (programsFile == null || programsFile.isEmpty()) {
      throw new InvalidFileHttpException("File is empty.");
    }

    List<ProgramCreationCsvRow> programBeans = getCsvBeansFromFile(programsFile);
    validateProgramBeans(programBeans);

    log.info("Creating multiple programs in TI");
    sendProgramsToTi(programBeans);
  }

  public void baseRateBulkCreation(MultipartFile baseRateFile, MaintenanceType maintenanceType) {
    if (baseRateFile == null || baseRateFile.isEmpty()) {
      throw new InvalidFileHttpException("File is empty");
    }

    List<BaseRateCsvRow> baseRateBeans = getCsvBaseRateBeansFromFile(baseRateFile);
    validateBaseRateBeans(baseRateBeans);

    log.info("Massive load of base rate on Ti");
    sendBaseRateToTi(baseRateBeans, maintenanceType);
  }

  private void validateProgramBeans(List<ProgramCreationCsvRow> programBeans)
      throws CsvValidationException {

    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();

    List<String> fileErrors = new ArrayList<>();
    int rowNumber = 1;

    for (ProgramCreationCsvRow program : programBeans) {
      Set<ConstraintViolation<ProgramCreationCsvRow>> violations = validator.validate(program);

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

  private void sendProgramsToTi(List<ProgramCreationCsvRow> programBeans) {
    for (ProgramCreationCsvRow program : programBeans) {
      var programRequest = programMapper.mapRowToItemRequest(program);
      producerTemplate.sendBody(uriFtiOutgoingFrom, programRequest);
    }
  }

  private List<ProgramCreationCsvRow> getCsvBeansFromFile(MultipartFile programsFile) {
    try (BufferedReader bufferedReader =
        new BufferedReader(new InputStreamReader(programsFile.getInputStream()))) {
      return new CsvToBeanBuilder<ProgramCreationCsvRow>(bufferedReader)
          .withType(ProgramCreationCsvRow.class)
          .withIgnoreEmptyLine(true)
          .build()
          .parse();
    } catch (IOException e) {
      throw new InvalidFileHttpException("Could not process file");
    }
  }

  private List<BaseRateCsvRow> getCsvBaseRateBeansFromFile(MultipartFile baseRateFile) {
    try(BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(baseRateFile.getInputStream()))) {

      return new CsvToBeanBuilder<BaseRateCsvRow>(bufferedReader)
          .withType(BaseRateCsvRow.class)
          .withIgnoreEmptyLine(true)
          .withSeparator(',')
          .build()
          .parse();
    } catch (IOException e) {
      throw new InvalidFileHttpException("Could not process File");
    }
  }

  private void validateBaseRateBeans(List<BaseRateCsvRow> baseRateBeans)
      throws CsvValidationException {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    Validator validator = factory.getValidator();

    List<String> fileErrors = new ArrayList<>();
    int rowNumber = 1;

    for (BaseRateCsvRow baseRate : baseRateBeans) {
      Set<ConstraintViolation<BaseRateCsvRow>> violations = validator.validate(baseRate);
      if (!violations.isEmpty()) {
        int line = rowNumber;
        List<String> errors =
            violations.stream()
                .map(v -> String.format("[Row #%d] %s", line, v.getMessage()))
                .toList();

        fileErrors.addAll(errors);
      }

      rowNumber++;
    }

    if (!fileErrors.isEmpty()) {
      throw new CsvValidationException("Base rate file has inconsistencies", fileErrors);
    }
  }

  private void sendBaseRateToTi(List<BaseRateCsvRow> baseRateBeans, MaintenanceType maintenanceType) {
    for (BaseRateCsvRow baseRate : baseRateBeans) {
      var baseRateRequest = baseRateMapper.mapRowToItemRequest(baseRate, maintenanceType);
      producerTemplate.sendBody( uriFtiOutgoingFrom, baseRateRequest);
    }
  }
}
