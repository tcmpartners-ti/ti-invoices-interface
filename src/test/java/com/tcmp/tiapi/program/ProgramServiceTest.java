package com.tcmp.tiapi.program;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tcmp.tiapi.customer.mapper.CounterPartyMapper;
import com.tcmp.tiapi.customer.model.CounterParty;
import com.tcmp.tiapi.customer.model.CounterPartyRole;
import com.tcmp.tiapi.customer.repository.CounterPartyRepository;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.model.InterestTier;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.program.repository.InterestTierRepository;
import com.tcmp.tiapi.program.repository.ProgramRepository;
import com.tcmp.tiapi.program.service.ProgramService;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ProgramServiceTest {
  @Mock private InterestTierRepository interestTierRepository;
  @Mock private ProgramRepository programRepository;
  @Mock private CounterPartyRepository counterPartyRepository;
  @Mock private ProgramMapper programMapper;

  @Captor private ArgumentCaptor<String> programIdArgumentCaptor;
  @Captor private ArgumentCaptor<Program> programArgumentCaptor;
  @Captor private ArgumentCaptor<Long> programPkArgumentCaptor;
  @Captor private ArgumentCaptor<Character> counterPartyRoleArgumentCaptor;
  @Captor private ArgumentCaptor<PageRequest> pageRequestArgumentCaptor;

  @InjectMocks private ProgramService programService;

  @BeforeEach
  void setUp() {
    var counterPartyMapper = Mappers.getMapper(CounterPartyMapper.class);
    ReflectionTestUtils.setField(programService, "counterPartyMapper", counterPartyMapper);
  }

  @Test
  void getProgramById_itCanGetProgramById() {
    String programUuid = "mockUuid";

    when(programRepository.findById(anyString()))
        .thenReturn(Optional.of(Program.builder().pk(1L).build()));
    when(interestTierRepository.findByProgrammeId(anyLong()))
        .thenReturn(Optional.of(InterestTier.builder().rate(BigDecimal.TEN).build()));

    programService.getProgramById(programUuid);

    verify(programRepository).findById(programUuid);
  }

  @Test
  void getProgramById_itShouldNotChangeProgramUuidWhenInvokingRepository() {
    String expectedProgramId = "mockUuid";

    when(programRepository.findById(anyString()))
        .thenReturn(Optional.of(Program.builder().pk(1L).build()));
    programService.getProgramById(expectedProgramId);

    verify(programRepository).findById(programIdArgumentCaptor.capture());
    String capturedUuid = programIdArgumentCaptor.getValue();

    assertEquals(capturedUuid, expectedProgramId);
  }

  @Test
  void getProgramById_itShouldThrowExceptionWhenProgramNotFoundByUuid() {
    String uuid = "mockUuid";

    when(programRepository.findById(uuid)).thenReturn(Optional.empty());

    assertThrows(NotFoundHttpException.class, () -> programService.getProgramById(uuid));
  }

  @Test
  void getProgramById_itShouldMapEntityToDto() {
    String expectedProgramId = "123";

    when(programRepository.findById(anyString()))
        .thenReturn(Optional.of(Program.builder().id(expectedProgramId).build()));
    when(programMapper.mapEntityToDTO(any(Program.class), any()))
        .thenReturn(ProgramDTO.builder().id(expectedProgramId).build());

    programService.getProgramById(expectedProgramId);

    verify(programMapper).mapEntityToDTO(programArgumentCaptor.capture(), any());
    assertEquals(expectedProgramId, programArgumentCaptor.getValue().getId());
  }

  @Test
  void getProgramSellersById_itShouldThrowExceptionWhenProgramNotFound() {
    String expectedProgramId = "IDEAL01";
    PageParams expectedPageParams = new PageParams();

    when(programRepository.findById(anyString())).thenReturn(Optional.empty());

    assertThrows(
        NotFoundHttpException.class,
        () -> programService.getProgramSellersById(expectedProgramId, expectedPageParams));
  }

  @Test
  void getProgramSellersById_itShouldCallCounterPartyRepository() {
    Long expectedProgramPk = 1L;
    String expectedProgramId = "IDEAL01";
    Character expectedCounterPartyRole = CounterPartyRole.SELLER.getValue();
    Program expectedProgram = Program.builder().pk(expectedProgramPk).id(expectedProgramId).build();
    List<CounterParty> expectedCounterParties =
        List.of(CounterParty.builder().id(1L).mnemonic("1722466420").build());

    when(programRepository.findById(anyString())).thenReturn(Optional.of(expectedProgram));
    when(counterPartyRepository.findByProgrammePkAndRole(
            anyLong(), any(Character.class), any(PageRequest.class)))
        .thenReturn(new PageImpl<>(expectedCounterParties));

    PageParams pageParams = new PageParams();
    programService.getProgramSellersById(expectedProgramId, pageParams);

    verify(counterPartyRepository)
        .findByProgrammePkAndRole(
            programPkArgumentCaptor.capture(),
            counterPartyRoleArgumentCaptor.capture(),
            pageRequestArgumentCaptor.capture());

    assertEquals(expectedProgramPk, programPkArgumentCaptor.getValue());
    assertEquals(expectedCounterPartyRole, counterPartyRoleArgumentCaptor.getValue());
    assertNotNull(pageRequestArgumentCaptor.getValue());
  }
}
