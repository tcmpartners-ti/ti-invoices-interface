package com.tcmp.tiapi.invoice;

import com.tcmp.tiapi.invoice.dto.request.InvoiceBulkCreationForm;
import com.tcmp.tiapi.invoice.dto.request.InvoiceCreationDTO;
import com.tcmp.tiapi.invoice.dto.request.InvoiceSearchParams;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.invoice.service.InvoiceService;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InvoiceController.class)
class InvoiceControllerTest {
  @Autowired MockMvc mockMvc;

  @MockBean private InvoiceService invoiceService;

  @Test
  void getInvoiceById_itShouldThrowExceptionWhenNotFound() throws Exception {
    long invoiceId = 1L;
    String expectedBody = "{\"status\":404,\"error\":\"Could not find invoice with id 1.\"}";

    when(invoiceService.getInvoiceById(anyLong()))
      .thenThrow(new NotFoundHttpException(
        String.format("Could not find invoice with id %s.", invoiceId)));

    mockMvc.perform(
        get(String.format("/invoices/%s", invoiceId))
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isNotFound())
      .andExpect(content().json(expectedBody, true));

    verify(invoiceService).getInvoiceById(invoiceId);
  }

  @Test
  void getInvoiceById_itShouldReturnInvoice() throws Exception {
    long invoiceId = 1L;
    String expectedBody = "{\"id\":1,\"invoiceNumber\":\"Invoice123\",\"buyerPartyId\":null,\"createFinanceEventId\":null,\"batchId\":null,\"buyer\":null,\"seller\":null,\"programme\":null,\"bulkPaymentMasterId\":null,\"subTypeCategory\":null,\"programType\":null,\"isApproved\":null,\"status\":null,\"detailsReceivedOn\":null,\"settlementDate\":null,\"issueDate\":null,\"isDisclosed\":null,\"isRecourse\":null,\"isDrawDownEligible\":null,\"preferredCurrencyCode\":null,\"isDeferCharged\":null,\"eligibilityReasonCode\":null,\"faceValue\":null,\"totalPaid\":null,\"outstanding\":null,\"advanceAvailable\":null,\"advanceAvailableEquivalent\":null,\"discountAdvance\":null,\"discountDeal\":null,\"detailsNotesForCustomer\":null,\"securityDetails\":null,\"taxDetails\":null}";

    when(invoiceService.getInvoiceById(anyLong()))
      .thenReturn(InvoiceDTO.builder()
        .id(invoiceId)
        .invoiceNumber("Invoice123")
        .build());

    mockMvc.perform(
        get(String.format("/invoices/%s", invoiceId))
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(content().json(expectedBody, true));

    verify(invoiceService).getInvoiceById(invoiceId);
  }

  @Test
  void searchInvoice_itShouldThrowExceptionWhenNotFound() throws Exception {
    String programId = "Program123";
    String expectedBody = "{\"status\":404,\"error\":\"Could not find a program with id Program123.\"}";

    when(invoiceService.searchInvoice(any(InvoiceSearchParams.class)))
      .thenThrow(new NotFoundHttpException(
        String.format("Could not find a program with id %s.", programId)));

    mockMvc.perform(
        get("/invoices/search")
          .param("programme", programId)
          .param("seller", "Seller123")
          .param("invoice", "Invoice123")
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isNotFound())
      .andExpect(content().json(expectedBody, true));
  }

  @Test
  void searchInvoice_itShouldReturnInvoice() throws Exception {
    long invoiceId = 1L;
    String expectedBody = "{\"id\":1,\"invoiceNumber\":\"Invoice123\",\"buyerPartyId\":null,\"createFinanceEventId\":null,\"batchId\":null,\"buyer\":null,\"seller\":null,\"programme\":null,\"bulkPaymentMasterId\":null,\"subTypeCategory\":null,\"programType\":null,\"isApproved\":null,\"status\":null,\"detailsReceivedOn\":null,\"settlementDate\":null,\"issueDate\":null,\"isDisclosed\":null,\"isRecourse\":null,\"isDrawDownEligible\":null,\"preferredCurrencyCode\":null,\"isDeferCharged\":null,\"eligibilityReasonCode\":null,\"faceValue\":null,\"totalPaid\":null,\"outstanding\":null,\"advanceAvailable\":null,\"advanceAvailableEquivalent\":null,\"discountAdvance\":null,\"discountDeal\":null,\"detailsNotesForCustomer\":null,\"securityDetails\":null,\"taxDetails\":null}";
    InvoiceSearchParams searchParams = InvoiceSearchParams.builder()
      .programme("Programme123")
      .seller("Seller123")
      .invoice("Invoice123")
      .build();

    when(invoiceService.searchInvoice(searchParams))
      .thenReturn(InvoiceDTO.builder()
        .id(invoiceId)
        .invoiceNumber("Invoice123")
        .build());

    mockMvc.perform(
        get("/invoices/search")
          .param("programme", searchParams.programme())
          .param("seller", searchParams.seller())
          .param("invoice", searchParams.invoice())
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(content().json(expectedBody, true));

    verify(invoiceService).searchInvoice(searchParams);
  }

  @Test
  void createInvoice_itShouldSendInvoiceToTI() throws Exception {
    String requestBody = "{\"context\":{\"customer\":\"1743067860001\",\"theirReference\":\"FINANCE25\",\"behalfOfBranch\":\"BPEC\"},\"anchorParty\":\"1743067860001\",\"anchorCurrentAccount\":\"2209677941\",\"programme\":\"Coffee1\",\"seller\":\"1790049795001\",\"buyer\":\"1743067860001\",\"invoiceNumber\":\"FINANCE25\",\"issueDate\":\"06-09-2023\",\"faceValue\":{\"amount\":1000,\"currency\":\"USD\"},\"outstandingAmount\":{\"amount\":1000,\"currency\":\"USD\"},\"settlementDate\":\"15-10-2023\"}\n";
    String expectedResponse = "{\"message\":\"Invoice sent to be created.\"};";

    mockMvc.perform(
        post("/invoices")
          .content(requestBody)
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(content().json(expectedResponse, true));


    verify(invoiceService).createSingleInvoiceInTi(any(InvoiceCreationDTO.class));
  }


  @Test
  void bulkCreateInvoices_itShouldValidateForm() throws Exception {
    MockMultipartFile mockFile = new MockMultipartFile(
      "invoicesFile",
      new byte[0]
    );
    InvoiceBulkCreationForm invalidForm = InvoiceBulkCreationForm.builder()
      .invoicesFile(mockFile)
      .batchId("InvalidBatch!d")
      .build();
    String expectedResponse = "{\"status\":400,\"error\":\"Could not validate the provided fields.\",\"errors\":[{\"field\":\"batchId\",\"error\":\"Only letters, numbers and underscores are allowed.\"}]}";

    mockMvc.perform(
        multipart("/invoices/bulk")
          .file(mockFile)
          .param("batchId", invalidForm.batchId())
      )
      .andExpect(status().is4xxClientError())
      .andExpect(content().json(expectedResponse, true));

    verify(invoiceService, never()).createMultipleInvoicesInTi(
      any(MultipartFile.class),
      anyString()
    );
  }

  @Test
  void bulkCreateInvoices_itShouldSendInvoicesToTI() throws Exception {
    MockMultipartFile mockFile = new MockMultipartFile(
      "invoicesFile",
      "1,2,3".getBytes()
    );
    InvoiceBulkCreationForm form = InvoiceBulkCreationForm.builder()
      .invoicesFile(mockFile)
      .batchId("Batch123")
      .build();
    String expectedResponse = "{\"message\":\"Invoices sent to be created.\"}";

    mockMvc.perform(
        multipart("/invoices/bulk")
          .file(mockFile)
          .param("batchId", form.batchId())
      )
      .andExpect(status().isOk())
      .andExpect(content().json(expectedResponse, true));

    verify(invoiceService).createMultipleInvoicesInTi(
      any(MultipartFile.class),
      anyString()
    );
  }
}
