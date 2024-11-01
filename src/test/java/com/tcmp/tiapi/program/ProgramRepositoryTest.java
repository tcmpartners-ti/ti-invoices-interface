package com.tcmp.tiapi.program;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.program.repository.ProgramRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
class ProgramRepositoryTest {
  @Autowired
  private ProgramRepository testedProgramRepository;

  @AfterEach
  void tearDown() {
    testedProgramRepository.deleteAll();
  }

  @Test
  void findById_itShouldFindProgramByIdWhenProgramExists() {
    Long expectedPk = 10_000L;
    String expectedId = "f47ac10b-58cc-4372-a567-0e02b2";
    Program expectedProgram = Program.builder()
      .pk(expectedPk)
      .id(expectedId)
      .build();
    testedProgramRepository.save(expectedProgram);

    Optional<Program> actualResult = testedProgramRepository.findById(expectedId);

    assertTrue(actualResult.isPresent());
    assertEquals(expectedId, actualResult.get().getId());
    assertEquals(expectedPk, actualResult.get().getPk());
  }

  @Test
  void findById_itShouldBeEmptyWhenProgramDoesntExist() {
    String nonExistingId = "2c6b96b9-5db0-44b1-85f1-4d2cb5d45e64";
    Optional<Program> actualResult = testedProgramRepository.findById(nonExistingId);

    var expectedResult = Optional.empty();
    assertThat(actualResult).isEqualTo(expectedResult);
  }

  @Test
  void findAllByCustomerMnemonic_itShouldHaveContentsWhenProgramsAreFound() {
    List<Program> programs = List.of(
      Program.builder()
        .pk(10_000L)
        .id("f47ac10b-58cc-4372-a567-0e02b1")
        .customerMnemonic("SUPER")
        .build(),
      Program.builder()
        .pk(10_001L)
        .id("f47ac10b-58cc-4372-a567-0e02b2")
        .customerMnemonic("SUPER")
        .build(),
      Program.builder()
        .pk(10_002L)
        .id("f47ac10b-58cc-4372-a567-0e02b2")
        .customerMnemonic("TIA")
        .build()
    );
    testedProgramRepository.saveAll(programs);

    Page<Program> programsPage = testedProgramRepository.findAllByCustomerMnemonic(
      "SUPER", PageRequest.of(0, 5));

    int expectedContentSize = 2;
    assertEquals(expectedContentSize, programsPage.getContent().size());
  }

  @Test
  void findAllByCustomerMnemonic_itShouldHaveEmptyContentsIfNoProgramsAreFound() {
    Page<Program> programsPage = testedProgramRepository.findAllByCustomerMnemonic(
      "SUPER", PageRequest.of(0, 5));

    int expectedContentSize = 0;
    assertEquals(expectedContentSize, programsPage.getContent().size());
  }
}
