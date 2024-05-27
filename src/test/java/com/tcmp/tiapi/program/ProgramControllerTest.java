package com.tcmp.tiapi.program;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.tcmp.tiapi.customer.dto.request.CounterPartyDTO;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.service.ProgramBatchOperationsService;
import com.tcmp.tiapi.program.service.ProgramService;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResult;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResultMeta;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = ProgramController.class)
class ProgramControllerTest {
  @Autowired MockMvc mockMvc;

  @MockBean private ProgramService programService;
  @MockBean private ProgramBatchOperationsService programBatchOperationsService;

  @Test
  void getProgramById_itShouldReturnNotFound() throws Exception {
    var programId = "notFound";
    var expectedBody = "{\"status\":404,\"error\":\"Could not find a program with id notFound.\"}";

    when(programService.getProgramById(anyString()))
        .thenThrow(
            new NotFoundHttpException(
                String.format("Could not find a program with id %s.", programId)));

    mockMvc
        .perform(
            get(String.format("/programs/%s", programId)).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNotFound())
        .andExpect(content().json(expectedBody, true));
  }

  @Test
  void getProgramById_itShouldReturnProgram() throws Exception {
    var programId = "123";
    var expectedBody =
        "{\"id\":\"123\",\"description\":null,\"customer\":null,\"startDate\":null,\"expiryDate\":null,\"type\":null,\"creditLimit\":null,\"status\":null,\"extraFinancingDays\":null,\"interestRate\":null}";

    when(programService.getProgramById(anyString()))
        .thenReturn(ProgramDTO.builder().id("123").build());

    mockMvc
        .perform(
            get(String.format("/programs/%s", programId)).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json(expectedBody, true));
  }

  @Test
  void getProgramSellersById_itShouldReturnProgramSellers() throws Exception {
    var programId = "123";

    when(programService.getProgramSellersById(anyString(), any(PageParams.class)))
        .thenReturn(
            PaginatedResult.<CounterPartyDTO>builder()
                .data(List.of(CounterPartyDTO.builder().build()))
                .meta(
                    PaginatedResultMeta.builder()
                        .totalItems(1)
                        .isLastPage(false)
                        .totalPages(1)
                        .build())
                .build());

    var expectedBody =
        "{\"data\":[{\"mnemonic\":null,\"name\":null,\"address\":null,\"branch\":null,\"status\":null,\"invoiceLimit\":null}],\"meta\":{\"isLastPage\":false,\"totalPages\":1,\"totalItems\":1}}";

    mockMvc
        .perform(
            get(String.format("/programs/%s/sellers", programId))
                .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json(expectedBody, true));
  }

  @Test
  void createMultiplePrograms_itShouldCreateMultiplePrograms() throws Exception {
    var expectedBody = "{\"status\":200,\"message\":\"Programs sent to be created\"}";

    doNothing().when(programBatchOperationsService).createMultipleProgramsInTi(any());

    mockMvc
        .perform(post("/programs/bulk").contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(content().json(expectedBody, true));
  }
}
