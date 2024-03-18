package com.tcmp.tiapi.customer.controller;

import com.tcmp.tiapi.customer.dto.response.CustomerBulkOperationResponse;
import com.tcmp.tiapi.customer.service.CustomerBatchOperationsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {
  private final CustomerBatchOperationsService customerBatchOperationsService;

  @PostMapping("bulk")
  public CustomerBulkOperationResponse createMultipleCustomers(MultipartFile customersFile) {
    customerBatchOperationsService.createMultipleCustomersInTi(customersFile);

    return new CustomerBulkOperationResponse(HttpStatus.OK.value(), "Customers sent to be created");
  }

  @DeleteMapping("bulk")
  public CustomerBulkOperationResponse deleteMultipleCustomers(MultipartFile customersFile) {
    customerBatchOperationsService.deleteMultipleCustomersInTi(customersFile);

    return new CustomerBulkOperationResponse(HttpStatus.OK.value(), "Customers sent to be deleted");
  }
}
