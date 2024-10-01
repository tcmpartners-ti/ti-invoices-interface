package com.tcmp.tiapi.program;

import com.tcmp.tiapi.customer.dto.request.CounterPartyDTO;
import com.tcmp.tiapi.program.dto.request.ProgramSellersDTO;
import com.tcmp.tiapi.program.dto.response.BaseRateOperationResponse;
import com.tcmp.tiapi.program.dto.response.ProgramBulkOperationResponse;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.service.ProgramBatchOperationsService;
import com.tcmp.tiapi.program.service.ProgramService;
import com.tcmp.tiapi.shared.FieldValidationRegex;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResult;
import com.tcmp.tiapi.ti.dto.MaintenanceType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("programs")
@RequiredArgsConstructor
@Validated
@Tag(name = "Programs", description = "Defines the programs operations.")
public class ProgramController {
  private final ProgramService programService;
  private final ProgramBatchOperationsService programBatchOperationsService;

  @GetMapping(path = "{programId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Get a program by its identifier.")
  @Parameter(
      name = "programId",
      description = "Program identifier.",
      schema = @Schema(type = "string"),
      in = ParameterIn.PATH,
      example = "ASEGURADORASUR")
  public ProgramDTO getProgramById(
      @PathVariable
          @Valid
          @NotNull(message = "This field is required.")
          @Size(max = 35, message = "This field must have up to 35 characters")
          @Pattern(
              regexp = FieldValidationRegex.AVOID_SPECIAL_CHARACTERS,
              message = "Special characters are not allowed")
          String programId) {
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
      @PathVariable
          @Valid
          @NotNull(message = "This field is required.")
          @Size(max = 35, message = "This field must have up to 35 characters")
          @Pattern(
              regexp = FieldValidationRegex.AVOID_SPECIAL_CHARACTERS,
              message = "Special characters are not allowed")
          String programId,
      @Parameter(hidden = true) @Valid PageParams pageParams) {
    return programService.getProgramSellersById(programId, pageParams);
  }

  @PostMapping("bulk")
  public ProgramBulkOperationResponse createMultiplePrograms(MultipartFile programsFile) {
    programBatchOperationsService.createMultipleProgramsInTi(programsFile);
    return new ProgramBulkOperationResponse(HttpStatus.OK.value(), "Programs sent to be created");
  }

  @PostMapping("base-rate/bulk")
  public BaseRateOperationResponse loadBaseRate(MultipartFile baseRateFile) {
    programBatchOperationsService.baseRateBulkCreation(baseRateFile, MaintenanceType.INSERT);
    return new BaseRateOperationResponse(HttpStatus.OK.value(), "Base Rate massive load sent");
  }

  @PutMapping("base-rate/bulk")
  public BaseRateOperationResponse updateBaseRate(MultipartFile baseRateFile) {
    programBatchOperationsService.baseRateBulkCreation(baseRateFile, MaintenanceType.UPDATE);
    return new BaseRateOperationResponse(HttpStatus.OK.value(), "Base Rate massive update sent");
  }

  @GetMapping(path = "{programId}/full-info", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Get a program's complete information with sellers by it's identifier")
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
  public ProgramSellersDTO getProgramFullInfo(
      @PathVariable
          @Valid
          @NotNull(message = "This filed is required.")
          @Size(max = 35, message = "This field must have up to 35 characters")
          @Pattern(
              regexp = FieldValidationRegex.AVOID_SPECIAL_CHARACTERS,
              message = "Special characters are not allowed")
          String programId,
      @Parameter(hidden = true) @Valid PageParams pageParams) {
    return programService.getFullProgramInformationById(programId, pageParams);
  }
}
