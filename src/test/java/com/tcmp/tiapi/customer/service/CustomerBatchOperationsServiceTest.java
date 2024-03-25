package com.tcmp.tiapi.customer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

import com.tcmp.tiapi.customer.dto.csv.CustomerCreationCSVRow;
import com.tcmp.tiapi.customer.mapper.CustomerMapper;
import com.tcmp.tiapi.shared.exception.CsvValidationException;
import com.tcmp.tiapi.shared.exception.InvalidFileHttpException;
import com.tcmp.tiapi.ti.dto.MaintenanceType;
import java.util.List;
import org.apache.camel.ProducerTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CustomerBatchOperationsServiceTest {
  @Mock private ProducerTemplate producerTemplate;
  @Mock private CustomerMapper customerMapper;

  @Captor private ArgumentCaptor<MaintenanceType> maintenanceTypeArgumentCaptor;
  @Captor private ArgumentCaptor<CustomerCreationCSVRow> customerCreationCSVRowArgumentCaptor;

  @InjectMocks private CustomerBatchOperationsService customerBatchOperationsService;

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(
        customerBatchOperationsService, "uriFtiOutgoingFrom", "direct:messi");
  }

  @Test
  void createMultipleCustomersInTi_itShouldHandleEmptyFiles() {
    var emptyMultiPartFile = new MockMultipartFile("emptyFile", new byte[0]);

    assertThrows(
        InvalidFileHttpException.class,
        () -> customerBatchOperationsService.createMultipleCustomersInTi(emptyMultiPartFile));
  }

  @Test
  void createMultipleCustomersInTi_itShouldHandleInvalidCustomersFiles() {
    var rawContent =
        "SourceBankingBusiness,Branch,Mnemonic,BankCode,Number,Type,FullName,ShortName,Address,Phone,Email,AccountType,AccountNumber,AccountCurrency,AccountDateOpened\n"
            + "BPCH,BPEC,FAIL1722466530001,0003,845139,SDC,Agatha Chirstie,A. Christie,UK,593987079645,achristie@mail.com,CA,CC2100307805,USD,01-04-2024";
    var customersFile =
        new MockMultipartFile("customersFile", "customers.csv", "text/csv", rawContent.getBytes());

    var csvValidationException =
        assertThrows(
            CsvValidationException.class,
            () -> customerBatchOperationsService.createMultipleCustomersInTi(customersFile));

    var expectedErrorMessage = "Customer file has inconsistencies";
    var expectedFieldErrors =
        List.of(
            "[Row #1] Mnemonic must be numeric",
            "[Row #1] Mnemonic must be between 10 and 13 characters");

    assertEquals(expectedErrorMessage, csvValidationException.getMessage());
    assertEquals(expectedFieldErrors, csvValidationException.getFieldErrors());
  }

  @Test
  void createMultipleCustomersInTi_itShouldHandleCustomersFiles() {
    var rawContent =
        "SourceBankingBusiness,Branch,Mnemonic,BankCode,Number,Type,FullName,ShortName,Address,Phone,Email,AccountType,AccountNumber,AccountCurrency,AccountDateOpened\n"
            + "BPCH,BPEC,1722466530001,0003,845139,SDC,Agatha Chirstie,A. Christie,UK,593987079645,achristie@mail.com,CA,CC2100307805,USD,01-04-2024";
    var customersFile =
        new MockMultipartFile("customersFile", "customers.csv", "text/csv", rawContent.getBytes());

    customerBatchOperationsService.createMultipleCustomersInTi(customersFile);

    verify(producerTemplate).asyncSendBody(anyString(), any());
    verify(customerMapper)
        .mapCustomerAndAccountToBulkRequest(
            maintenanceTypeArgumentCaptor.capture(),
            customerCreationCSVRowArgumentCaptor.capture());
    assertEquals(MaintenanceType.DEFINE, maintenanceTypeArgumentCaptor.getValue());
    assertEquals("1722466530001", customerCreationCSVRowArgumentCaptor.getValue().getMnemonic());
  }

  @Test
  void deleteMultipleCustomersInTi_itShouldHandleEmptyFiles() {
    var emptyMultiPartFile = new MockMultipartFile("emptyFile", new byte[0]);

    assertThrows(
        InvalidFileHttpException.class,
        () -> customerBatchOperationsService.deleteMultipleCustomersInTi(emptyMultiPartFile));
  }

  @Test
  void deleteMultipleCustomersInTi_itShouldHandleInvalidCustomersFiles() {
    var rawContent =
        "SourceBankingBusiness,Branch,Mnemonic,BankCode,Number,Type,FullName,ShortName,Address,Phone,Email,AccountType,AccountNumber,AccountCurrency,AccountDateOpened\n"
            + "BPCH,BPEC,FAIL1722466530001,0003,845139,SDC,Agatha Chirstie,A. Christie,UK,593987079645,achristie@mail.com,CA,CC2100307805,USD,01-04-2024";
    var customersFile =
        new MockMultipartFile("customersFile", "customers.csv", "text/csv", rawContent.getBytes());

    var csvValidationException =
        assertThrows(
            CsvValidationException.class,
            () -> customerBatchOperationsService.deleteMultipleCustomersInTi(customersFile));

    var expectedErrorMessage = "Customer file has inconsistencies";
    var expectedFieldErrors =
        List.of(
            "[Row #1] Mnemonic must be numeric",
            "[Row #1] Mnemonic must be between 10 and 13 characters");

    assertEquals(expectedErrorMessage, csvValidationException.getMessage());
    assertEquals(expectedFieldErrors, csvValidationException.getFieldErrors());
  }

  @Test
  void deleteMultipleCustomersInTi_itShouldHandleCustomersFiles() {
    var rawContent =
        "SourceBankingBusiness,Branch,Mnemonic,BankCode,Number,Type,FullName,ShortName,Address,Phone,Email,AccountType,AccountNumber,AccountCurrency,AccountDateOpened\n"
            + "BPCH,BPEC,1722466530001,0003,845139,SDC,Agatha Chirstie,A. Christie,UK,593987079645,achristie@mail.com,CA,CC2100307805,USD,01-04-2024";
    var customersFile =
        new MockMultipartFile("customersFile", "customers.csv", "text/csv", rawContent.getBytes());

    customerBatchOperationsService.deleteMultipleCustomersInTi(customersFile);

    verify(producerTemplate).asyncSendBody(anyString(), any());
    verify(customerMapper)
        .mapCustomerAndAccountToBulkRequest(
            maintenanceTypeArgumentCaptor.capture(),
            customerCreationCSVRowArgumentCaptor.capture());
    assertEquals(MaintenanceType.DELETE, maintenanceTypeArgumentCaptor.getValue());
    assertEquals("1722466530001", customerCreationCSVRowArgumentCaptor.getValue().getMnemonic());
  }
}
