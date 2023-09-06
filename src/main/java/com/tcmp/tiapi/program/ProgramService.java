package com.tcmp.tiapi.program;

import com.tcmp.tiapi.customer.dto.CounterPartyDTO;
import com.tcmp.tiapi.customer.mapper.CounterPartyMapper;
import com.tcmp.tiapi.customer.model.CounterParty;
import com.tcmp.tiapi.customer.model.CounterPartyRole;
import com.tcmp.tiapi.customer.repository.CounterPartyRepository;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.dto.response.PaginatedResult;
import com.tcmp.tiapi.shared.dto.response.PaginatedResultMeta;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProgramService {
  private final ProgramRepository programRepository;
  private final CounterPartyRepository counterPartyRepository;
  private final ProgramMapper programMapper;
  private final CounterPartyMapper counterPartyMapper;

  public ProgramDTO getProgramById(String programId) {
    Program program = findProgramByIdOrThrowNotFoundException(programId);
    return programMapper.mapEntityToDTO(program);
  }

  public PaginatedResult<CounterPartyDTO> getProgramSellersById(String programId, PageParams pageParams) {
    Program program = findProgramByIdOrThrowNotFoundException(programId);

    Page<CounterParty> sellerCounterPartiesPage = counterPartyRepository.findByProgrammePkAndRole(
      program.getPk(),
      CounterPartyRole.SELLER.getValue(),
      PageRequest.of(pageParams.getPage(), pageParams.getSize())
    );

    return PaginatedResult.<CounterPartyDTO>builder()
      .data(counterPartyMapper.mapEntitiesToDTOs(sellerCounterPartiesPage.getContent()))
      .meta(PaginatedResultMeta.builder()
        .isLastPage(sellerCounterPartiesPage.isLast())
        .totalItems(sellerCounterPartiesPage.getTotalElements())
        .totalPages(sellerCounterPartiesPage.getTotalPages())
        .build())
      .build();
  }

  private Program findProgramByIdOrThrowNotFoundException(String programId) {
    return programRepository.findById(programId)
      .orElseThrow(() -> new NotFoundHttpException(
        String.format("Could not find a program with id %s.", programId)));
  }
}
