package com.tcmp.tiapi.program;

import com.tcmp.tiapi.customer.dto.request.CounterPartyDTO;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResult;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResultMeta;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProgramController.class)
class ProgramControllerTest {
  @Autowired MockMvc mockMvc;

  @MockBean private ProgramService programService;

  @Test
  void getProgramById_itShouldReturnNotFound() throws Exception {
    String programId = "notFound";
    String expectedBody = "{\"status\":404,\"error\":\"Could not find a program with id notFound.\"}";

    when(programService.getProgramById(anyString()))
      .thenThrow(new NotFoundHttpException(
        String.format("Could not find a program with id %s.", programId)));

    mockMvc.perform(
        get(String.format("/programs/%s", programId))
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isNotFound())
      .andExpect(content().json(expectedBody, true));
  }

  @Test
  void getProgramById_itShouldReturnProgram() throws Exception {
    String programId = "123";
    String expectedBody = "{\"id\":\"123\",\"description\":null,\"customer\":null,\"startDate\":null,\"expiryDate\":null,\"type\":null,\"creditLimit\":null,\"status\":null}";

    when(programService.getProgramById(anyString()))
      .thenReturn(ProgramDTO.builder()
        .id("123")
        .build());

    mockMvc.perform(
        get(String.format("/programs/%s", programId))
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(content().json(expectedBody, true));
  }

  @Test
  void getProgramSellersById_itShouldReturnProgramSellers() throws Exception {
    var programId = "123";

    when(programService.getProgramSellersById(anyString(), any(PageParams.class)))
      .thenReturn(PaginatedResult.<CounterPartyDTO>builder()
        .data(List.of(CounterPartyDTO.builder().build()))
        .meta(PaginatedResultMeta.builder()
          .totalItems(1)
          .isLastPage(false)
          .totalPages(1)
          .build())
        .build());

    String expectedBody = "{\"data\":[{\"mnemonic\":null,\"name\":null,\"address\":null,\"branch\":null,\"status\":null,\"invoiceLimit\":null}],\"meta\":{\"isLastPage\":false,\"totalPages\":1,\"totalItems\":1}}";

    mockMvc.perform(
        get(String.format("/programs/%s/sellers", programId))
          .contentType(MediaType.APPLICATION_JSON)
      )
      .andExpect(status().isOk())
      .andExpect(content().json(expectedBody, true));
  }
}
