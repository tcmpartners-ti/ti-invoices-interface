package com.tcmp.tiapi.customer;

import com.tcmp.tiapi.program.ProgramMapper;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.dto.response.PaginatedResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customers [WIP]", description = "Defines the customers operations.")
public class CustomerController {
  private final CustomerService customerService;
  private final ProgramMapper programMapper;

  @GetMapping("{customerMnemonic}/programs")
  public ResponseEntity<PaginatedResult<List<ProgramDTO>>> getCustomerProgramsByMnemonic(PageParams pageParams, @PathVariable String customerMnemonic) {
    Page<Program> programsPage = customerService.getCustomerPrograms(customerMnemonic, pageParams);

    return ResponseEntity.ok(PaginatedResult.<List<ProgramDTO>>builder()
      .data(programsPage.get().map(programMapper::mapEntityToDTO).toList())
      .meta(Map.of(
        "pagination", Map.of(
          "isLastPage", programsPage.isLast(),
          "totalPages", programsPage.getTotalPages()
        )
      ))
      .build());
  }
}
