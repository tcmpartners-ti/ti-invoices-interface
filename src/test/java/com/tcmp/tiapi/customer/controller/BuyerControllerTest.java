package com.tcmp.tiapi.customer.controller;

import com.tcmp.tiapi.customer.service.BuyerService;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResult;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
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

@WebMvcTest(controllers = BuyerController.class)
class BuyerControllerTest {
  @Autowired MockMvc mockMvc;

  @MockBean private BuyerService buyerService;

  @Test
  void getBuyerProgramsByMnemonic_itShouldProcessRequest() throws Exception {

    String expectedBuyerMnemonic = "1722466421001";
    List<ProgramDTO> mockPrograms = List.of(
      ProgramDTO.builder()
        .id("Program123")
        .build()
    );
    String expectedResponseBody = "{\"data\":[{\"id\":\"Program123\",\"description\":null,\"customer\":null,\"startDate\":null,\"expiryDate\":null,\"type\":null,\"creditLimit\":null,\"status\":null}],\"meta\":null}";

    when(buyerService.getBuyerProgramsByMnemonic(anyString(), any(PageParams.class)))
      .thenReturn(PaginatedResult.<ProgramDTO>builder()
        .data(mockPrograms)
        .build());

    mockMvc.perform(
        get(String.format("/buyers/%s/programs", expectedBuyerMnemonic))
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(content().json(expectedResponseBody));

    verify(buyerService).getBuyerProgramsByMnemonic(
      anyString(),
      any(PageParams.class)
    );
  }

  @Test
  void getBuyerProgramsByMnemonic_itShouldReturnNotFoundMessage() throws Exception {
    String expectedBuyerMnemonic = "a";
    String expectedResponseErrorMessage = String.format("Could not find customer with mnemonic %s.", expectedBuyerMnemonic);
    String expectedResponseBody = "{\"status\":404,\"error\":\"" + expectedResponseErrorMessage + "\"}";

    when(buyerService.getBuyerProgramsByMnemonic(anyString(), any(PageParams.class)))
      .thenThrow(new NotFoundHttpException(expectedResponseErrorMessage));

    mockMvc.perform(
        get(String.format("/buyers/%s/programs", expectedBuyerMnemonic))
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isNotFound())
      .andExpect(content().json(expectedResponseBody, true));

    verify(buyerService).getBuyerProgramsByMnemonic(
      anyString(),
      any(PageParams.class)
    );
  }
}
