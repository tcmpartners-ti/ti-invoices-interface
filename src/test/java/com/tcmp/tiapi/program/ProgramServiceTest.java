package com.tcmp.tiapi.program;

import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgramServiceTest {
  @Mock
  private ProgramRepository programRepository;
  @Mock
  private ProgramMapper programMapper;

  private ProgramService testedProgramService;

  @BeforeEach
  void setUp() {
    testedProgramService = new ProgramService(
      programRepository,
      programMapper
    );
  }

  @Test
  void itCanGetProgramByUuid() {
    String programUuid = "mockUuid";

    when(programRepository.findById(anyString()))
      .thenReturn(Optional.of(Program.builder().pk(1L).build()));

    testedProgramService.getProgramById(programUuid);

    verify(programRepository).findById(programUuid);
  }

  @Test
  void itShouldNotChangeProgramUuidWhenInvokingRepository() {
    String expectedProgramUuid = "mockUuid";

    when(programRepository.findById(anyString()))
      .thenReturn(Optional.of(Program.builder().pk(1L).build()));
    testedProgramService.getProgramById(expectedProgramUuid);

    ArgumentCaptor<String> uuidCaptor = ArgumentCaptor.forClass(String.class);

    verify(programRepository).findById(uuidCaptor.capture());
    String capturedUuid = uuidCaptor.getValue();

    assertEquals(capturedUuid, expectedProgramUuid);
  }

  @Test
  void itShouldThrowExceptionWhenProgramNotFoundByUuid() {
    String uuid = "mockUuid";

    when(programRepository.findById(uuid))
      .thenReturn(Optional.empty());

    assertThrows(NotFoundHttpException.class,
      () -> testedProgramService.getProgramById(uuid));
  }

  @Test
  void itShouldMapEntityToDto() {
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

    ArgumentCaptor<Program> programCaptor = ArgumentCaptor.forClass(Program.class);
    verify(programMapper).mapEntityToDTO(programCaptor.capture());
    assertEquals(expectedProgramId, programCaptor.getValue().getId());
  }
}
