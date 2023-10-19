package com.tcmp.tiapi.customer.controller;

import com.tcmp.tiapi.customer.service.BuyerService;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("buyers")
@RequiredArgsConstructor
@Tag(name = "Buyers", description = "Defines the buyers (anchors) operations.")
public class BuyerController {
  private final BuyerService buyerService;

  @GetMapping(path = "{buyerMnemonic}/programs", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Get the buyer's (anchor's) programs by its mnemonic.")
  @Parameter(name = "page", description = "Page (0 based).", in = ParameterIn.QUERY, example = "0")
  @Parameter(name = "size", description = "Page size (items per page).", in = ParameterIn.QUERY, example = "10")
  public PaginatedResult<ProgramDTO> getBuyerProgramsByMnemonic(
    @Parameter(hidden = true) @Valid PageParams pageParams,
    @PathVariable String buyerMnemonic
  ) {
    return buyerService.getBuyerProgramsByMnemonic(buyerMnemonic, pageParams);
  }
}
