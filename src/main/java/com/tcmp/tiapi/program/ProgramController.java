package com.tcmp.tiapi.program;

import com.tcmp.tiapi.customer.dto.CounterPartyDTO;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("programs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Programs", description = "Defines the programs operations.")
public class ProgramController {
  private final ProgramService programService;

  @GetMapping(path = "{programId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Get a program by its identifier.")
  public ProgramDTO getProgramById(@PathVariable String programId) {
    return programService.getProgramById(programId);
  }

  @GetMapping(path = "{programId}/sellers", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Get a program's sellers by its identifier")
  public PaginatedResult<CounterPartyDTO> getProgramSellersById(
    @PathVariable String programId,
    PageParams pageParams
  ) {
    return programService.getProgramSellersById(
      programId,
      pageParams
    );
  }
}
