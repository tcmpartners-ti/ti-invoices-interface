package com.tcmp.tiapi.customer.controller;

import com.tcmp.tiapi.customer.dto.response.SearchSellerInvoicesParams;
import com.tcmp.tiapi.customer.service.SellerService;
import com.tcmp.tiapi.invoice.dto.response.InvoiceDTO;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResult;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResultMeta;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SellerController.class)
class SellerControllerTest {
  @Autowired private MockMvc mockMvc;

  @MockBean private SellerService sellerService;

  @Test
  void getSellerInvoicesByMnemonic_itShouldReturnSellerInvoices() throws Exception {
    String expectedSellerMnemonic = "1722466420001";
    String expectedBody =
        "{\"data\":[{\"id\":null,\"invoiceNumber\":\"01-001\",\"buyerPartyId\":null,\"createFinanceEventId\":null,\"batchId\":null,\"buyer\":null,\"seller\":null,\"programme\":null,\"bulkPaymentMasterId\":null,\"subTypeCategory\":null,\"programType\":null,\"isApproved\":null,\"status\":null,\"detailsReceivedOn\":null,\"settlementDate\":null,\"issueDate\":null,\"isDisclosed\":null,\"isRecourse\":null,\"isDrawDownEligible\":null,\"preferredCurrencyCode\":null,\"isDeferCharged\":null,\"eligibilityReasonCode\":null,\"faceValue\":null,\"totalPaid\":null,\"outstanding\":null,\"advanceAvailable\":null,\"advanceAvailableEquivalent\":null,\"discountAdvance\":null,\"discountDeal\":null,\"detailsNotesForCustomer\":null,\"securityDetails\":null,\"taxDetails\":null}],\"meta\":{\"isLastPage\":true,\"totalPages\":1,\"totalItems\":1}}";

    List<InvoiceDTO> mockInvoices = List.of(InvoiceDTO.builder().invoiceNumber("01-001").build());

    when(sellerService.getSellerInvoices(
            anyString(), any(SearchSellerInvoicesParams.class), any(PageParams.class)))
        .thenReturn(
            PaginatedResult.<InvoiceDTO>builder()
                .data(mockInvoices)
                .meta(
                    PaginatedResultMeta.builder()
                        .isLastPage(true)
                        .totalPages(1)
                        .totalItems(1)
                        .build())
                .build());

    mockMvc
        .perform(
            get(String.format("/sellers/%s/invoices", expectedSellerMnemonic))
                .param("status", "O")
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json(expectedBody, true));
    verify(sellerService)
        .getSellerInvoices(
            anyString(), any(SearchSellerInvoicesParams.class), any(PageParams.class));
  }
}
