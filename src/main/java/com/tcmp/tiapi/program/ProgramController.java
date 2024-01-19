package com.tcmp.tiapi.program;

import com.tcmp.tiapi.customer.dto.request.CounterPartyDTO;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
  @Parameter(
      name = "programId",
      description = "Program identifier.",
      schema = @Schema(type = "string"),
      in = ParameterIn.PATH,
      example = "ASEGURADORASUR")
  public ProgramDTO getProgramById(@PathVariable String programId) {
    return programService.getProgramById(programId);
  }

  @GetMapping(path = "{programId}/sellers", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Get a program's sellers by its identifier")
  @Parameter(
      name = "programId",
      description = "Program identifier.",
      schema = @Schema(type = "string"),
      in = ParameterIn.PATH,
      example = "IDEAL01")
  @Parameter(
      name = "page",
      description = "Page (0 based).",
      schema = @Schema(type = "number"),
      in = ParameterIn.QUERY,
      example = "0")
  @Parameter(
      name = "size",
      description = "Page size (items per page).",
      schema = @Schema(type = "number"),
      in = ParameterIn.QUERY,
      example = "10")
  public PaginatedResult<CounterPartyDTO> getProgramSellersById(
      @PathVariable String programId, @Parameter(hidden = true) @Valid PageParams pageParams) {
    return programService.getProgramSellersById(programId, pageParams);
  }
}
