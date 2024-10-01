package com.tcmp.tiapi.program.service;

import com.tcmp.tiapi.customer.dto.request.CounterPartyDTO;
import com.tcmp.tiapi.customer.dto.response.SellerInfoDTO;
import com.tcmp.tiapi.customer.dto.response.SellerToProgramRelationDTO;
import com.tcmp.tiapi.customer.mapper.CounterPartyMapper;
import com.tcmp.tiapi.customer.model.Account;
import com.tcmp.tiapi.customer.model.CounterParty;
import com.tcmp.tiapi.customer.model.CounterPartyRole;
import com.tcmp.tiapi.customer.repository.AccountRepository;
import com.tcmp.tiapi.customer.repository.CounterPartyRepository;
import com.tcmp.tiapi.program.dto.request.ProgramSellersDTO;
import com.tcmp.tiapi.program.mapper.ProgramMapper;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.model.InterestTier;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.program.repository.InterestTierRepository;
import com.tcmp.tiapi.program.repository.ProgramRepository;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResult;
import com.tcmp.tiapi.shared.dto.response.paginated.PaginatedResultMeta;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProgramService {
  private final InterestTierRepository interestTierRepository;
  private final ProgramRepository programRepository;
  private final CounterPartyRepository counterPartyRepository;
  private final ProgramMapper programMapper;
  private final CounterPartyMapper counterPartyMapper;
  private final AccountRepository accountRepository;

  public ProgramDTO getProgramById(String programId) {
    Program program = findProgramByIdOrThrowNotFoundException(programId);
    BigDecimal rate =
        interestTierRepository
            .findByProgrammeId(program.getPk())
            .map(InterestTier::getRate)
            .orElse(BigDecimal.ZERO);

    return programMapper.mapEntityToDTO(program, rate);
  }

  public PaginatedResult<CounterPartyDTO> getProgramSellersById(
      String programId, PageParams pageParams) {
    Program program = findProgramByIdOrThrowNotFoundException(programId);

    Page<CounterParty> sellerCounterPartiesPage =
        counterPartyRepository.findByProgrammePkAndRole(
            program.getPk(),
            CounterPartyRole.SELLER.getValue(),
            PageRequest.of(pageParams.getPage(), pageParams.getSize()));

    return PaginatedResult.<CounterPartyDTO>builder()
        .data(counterPartyMapper.mapEntitiesToDTOs(sellerCounterPartiesPage.getContent()))
        .meta(PaginatedResultMeta.from(sellerCounterPartiesPage))
        .build();
  }

  private Program findProgramByIdOrThrowNotFoundException(String programId) {
    return programRepository
        .findById(programId)
        .orElseThrow(
            () ->
                new NotFoundHttpException(
                    String.format("Could not find a program with id %s.", programId)));
  }

  public ProgramSellersDTO getFullProgramInformationById(String programId, PageParams pageParams) {
    Program program = findProgramByIdOrThrowNotFoundException(programId);
    BigDecimal rate =
        interestTierRepository
            .findByProgrammeId(program.getPk())
            .map(InterestTier::getRate)
            .orElse(BigDecimal.ZERO);
    ProgramDTO programInfo = programMapper.mapEntityToDTO(program, rate);

    Page<CounterParty> sellerCounterPartiesPage =
        counterPartyRepository.findByProgrammePkAndRole(
            program.getPk(),
            CounterPartyRole.SELLER.getValue(),
            PageRequest.of(pageParams.getPage(), pageParams.getSize()));


    PaginatedResult<SellerInfoDTO> paginatedResult =
        PaginatedResult.<SellerInfoDTO>builder()
            .data(counterPartyMapper.mapEntitiesToSellerInfoDTOs(sellerCounterPartiesPage.getContent()))
            .meta(PaginatedResultMeta.from(sellerCounterPartiesPage))
            .build();

    return ProgramSellersDTO.builder()
        .programInfo(programInfo)
        .sellersInfo(paginatedResult)
        .build();
  }
}
