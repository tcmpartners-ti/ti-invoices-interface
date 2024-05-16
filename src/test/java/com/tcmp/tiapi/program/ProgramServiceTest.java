package com.tcmp.tiapi.program;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.tcmp.tiapi.customer.mapper.CounterPartyMapper;
import com.tcmp.tiapi.customer.model.CounterParty;
import com.tcmp.tiapi.customer.model.CounterPartyRole;
import com.tcmp.tiapi.customer.repository.CounterPartyRepository;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.program.repository.ProgramRepository;
import com.tcmp.tiapi.program.service.ProgramService;
import com.tcmp.tiapi.shared.dto.request.PageParams;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class ProgramServiceTest {
  @Mock private ProgramRepository programRepository;
  @Mock private CounterPartyRepository counterPartyRepository;
  @Mock private ProgramMapper programMapper;
  @Mock private CounterPartyMapper counterPartyMapper;

  @Captor ArgumentCaptor<String> programIdArgumentCaptor;
  @Captor ArgumentCaptor<Program> programArgumentCaptor;
  @Captor ArgumentCaptor<Long> programPkArgumentCaptor;
  @Captor ArgumentCaptor<Character> counterPartyRoleArgumentCaptor;
  @Captor ArgumentCaptor<PageRequest> pageRequestArgumentCaptor;

  private ProgramService testedProgramService;

  @BeforeEach
  void setUp() {
    testedProgramService = new ProgramService(
      programRepository,
      counterPartyRepository,
      programMapper,
      counterPartyMapper
    );
  }

  @Test
  void getProgramById_itCanGetProgramByUuid() {
    String programUuid = "mockUuid";

    when(programRepository.findById(anyString()))
      .thenReturn(Optional.of(Program.builder().pk(1L).build()));

    testedProgramService.getProgramById(programUuid);

    verify(programRepository).findById(programUuid);
  }

  @Test
  void getProgramById_itShouldNotChangeProgramUuidWhenInvokingRepository() {
    String expectedProgramId = "mockUuid";

    when(programRepository.findById(anyString()))
      .thenReturn(Optional.of(Program.builder().pk(1L).build()));
    testedProgramService.getProgramById(expectedProgramId);

    verify(programRepository).findById(programIdArgumentCaptor.capture());
    String capturedUuid = programIdArgumentCaptor.getValue();

    assertEquals(capturedUuid, expectedProgramId);
  }

  @Test
  void getProgramById_itShouldThrowExceptionWhenProgramNotFoundByUuid() {
    String uuid = "mockUuid";

    when(programRepository.findById(uuid))
      .thenReturn(Optional.empty());

    assertThrows(NotFoundHttpException.class,
      () -> testedProgramService.getProgramById(uuid));
  }

  @Test
  void getProgramById_itShouldMapEntityToDto() {
    String expectedProgramId = "123";

    when(programRepository.findById(anyString()))
      .thenReturn(Optional.of(Program.builder()
        .id(expectedProgramId)
        .build()));
    when(programMapper.mapEntityToDTO(any(Program.class)))
      .thenReturn(ProgramDTO.builder()
        .id(expectedProgramId)
        .build());

    testedProgramService.getProgramById(expectedProgramId);

    verify(programMapper).mapEntityToDTO(programArgumentCaptor.capture());
    assertEquals(expectedProgramId, programArgumentCaptor.getValue().getId());
  }

  @Test
  void getProgramSellersById_itShouldThrowExceptionWhenProgramNotFound() {
    String expectedProgramId = "IDEAL01";
    PageParams expectedPageParams = new PageParams();

    when(programRepository.findById(anyString()))
      .thenReturn(Optional.empty());

    assertThrows(NotFoundHttpException.class,
      () -> testedProgramService.getProgramSellersById(expectedProgramId, expectedPageParams));
  }

  @Test
  void getProgramSellersById_itShouldCallCounterPartyRepository() {
    Long expectedProgramPk = 1L;
    String expectedProgramId = "IDEAL01";
    Character expectedCounterPartyRole = CounterPartyRole.SELLER.getValue();
    Program expectedProgram = Program.builder()
      .pk(expectedProgramPk)
      .id(expectedProgramId)
      .build();
    List<CounterParty> expectedCounterParties = List.of(
      CounterParty.builder()
        .id(1L)
        .mnemonic("1722466420")
        .build()
    );

    when(programRepository.findById(anyString()))
      .thenReturn(Optional.of(expectedProgram));
    when(counterPartyRepository.findByProgrammePkAndRole(
      anyLong(),
      any(Character.class),
      any(PageRequest.class)
    ))
      .thenReturn(new PageImpl<>(expectedCounterParties));

    PageParams pageParams = new PageParams();
    testedProgramService.getProgramSellersById(
      expectedProgramId,
      pageParams
    );

    verify(counterPartyRepository).findByProgrammePkAndRole(
      programPkArgumentCaptor.capture(),
      counterPartyRoleArgumentCaptor.capture(),
      pageRequestArgumentCaptor.capture()
    );

    assertEquals(expectedProgramPk, programPkArgumentCaptor.getValue());
    assertEquals(expectedCounterPartyRole, counterPartyRoleArgumentCaptor.getValue());
    assertNotNull(pageRequestArgumentCaptor.getValue());
  }
}
