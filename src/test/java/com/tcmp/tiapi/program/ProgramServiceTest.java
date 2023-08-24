package com.tcmp.tiapi.program;

import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import org.apache.camel.ProducerTemplate;
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
  private ProducerTemplate producerTemplate;

  private ProgramService testedProgramService;

  @BeforeEach
  void setUp() {
    testedProgramService = new ProgramService(
      programRepository,
      producerTemplate,
      new ProgramConfiguration()
    );
  }

  @Test
  void itCanGetProgramByUuid() {
    // Given
    String programUuid = "mockUuid";

    // When
    when(programRepository.findById(anyString()))
      .thenReturn(Optional.of(Program.builder().pk(1L).build()));

    testedProgramService.getProgramById(programUuid);

    //Then
    verify(programRepository).findById(programUuid);
  }

  @Test
  void itShouldNotChangeProgramUuidWhenInvokingRepository() {
    // Given
    String expectedProgramUuid = "mockUuid";

    // When
    when(programRepository.findById(anyString()))
      .thenReturn(Optional.of(Program.builder().pk(1L).build()));
    testedProgramService.getProgramById(expectedProgramUuid);

    // Then
    ArgumentCaptor<String> uuidCaptor = ArgumentCaptor.forClass(String.class);

    verify(programRepository).findById(uuidCaptor.capture());
    String capturedUuid = uuidCaptor.getValue();

    assertEquals(capturedUuid, expectedProgramUuid);
  }

  @Test
  void itShouldThrowExceptionWhenProgramNotFoundByUuid() {
    String uuid = "mockUuid";

    // When
    when(programRepository.findById(uuid))
      .thenReturn(Optional.empty());

    // Then
    assertThrows(NotFoundHttpException.class,
      () -> testedProgramService.getProgramById(uuid));
  }
}
