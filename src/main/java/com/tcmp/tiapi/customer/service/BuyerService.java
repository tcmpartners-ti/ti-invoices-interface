package com.tcmp.tiapi.customer.service;

import com.tcmp.tiapi.customer.repository.CustomerRepository;
import com.tcmp.tiapi.program.ProgramMapper;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.program.repository.InterestTierRepository;
import com.tcmp.tiapi.program.repository.ProgramRepository;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResult;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResultMeta;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BuyerService {
  private final InterestTierRepository interestTierRepository;
  private final CustomerRepository customerRepository;
  private final ProgramRepository programRepository;
  private final ProgramMapper programMapper;

  public PaginatedResult<ProgramDTO> getBuyerProgramsByMnemonic(
      String buyerMnemonic, PageParams pageParams) {
    if (!customerRepository.existsByIdMnemonic(buyerMnemonic)) {
      throw new NotFoundHttpException(
          String.format("Could not find customer with mnemonic %s.", buyerMnemonic));
    }

    Page<Program> programsPage =
        programRepository.findAllByCustomerMnemonic(
            buyerMnemonic, PageRequest.of(pageParams.getPage(), pageParams.getSize()));

    Set<Long> programIds = programsPage.stream().map(Program::getPk).collect(Collectors.toSet());
    Map<Long, BigDecimal> programsInterests =
        interestTierRepository.findByProgrammeIdIn(programIds).stream()
            .collect(
                Collectors.toMap(
                    t -> t.getInterest().getProgramId(),
                    t -> Optional.ofNullable(t.getRate()).orElse(BigDecimal.ZERO)));

    return PaginatedResult.<ProgramDTO>builder()
        .data(programMapper.mapEntitiesToDTOs(programsPage.getContent(), programsInterests))
        .meta(
            PaginatedResultMeta.builder()
                .isLastPage(programsPage.isLast())
                .totalPages(programsPage.getTotalPages())
                .totalItems(programsPage.getTotalElements())
                .build())
        .build();
  }
}
