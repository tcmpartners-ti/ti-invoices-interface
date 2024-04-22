package com.tcmp.tiapi.program;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.program.model.ProgramExtension;
import com.tcmp.tiapi.program.repository.ProgramExtensionRepository;
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
  @Autowired private ProgramRepository testedProgramRepository;

  @Autowired private ProgramExtensionRepository programExtensionRepository;

  @AfterEach
  void tearDown() {
    testedProgramRepository.deleteAll();
  }

  @Test
  void findById_itShouldFindProgramByIdWhenProgramExists() {
    var expectedPk = 10_000L;
    var expectedId = "f47ac10b-58cc-4372-a567-0e02b2";
    var expectedProgram = Program.builder().pk(expectedPk).id(expectedId).build();
    var extension =
        ProgramExtension.builder().programmeId(expectedId).extraFinancingDays(30).build();
    programExtensionRepository.save(extension);
    testedProgramRepository.save(expectedProgram);

    var actualResult = testedProgramRepository.findById(expectedId);

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
    var programs =
        List.of(
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
                .id("f47ac10b-58cc-4372-a567-0e02b3")
                .customerMnemonic("TIA")
                .build());
    var extensions =
        List.of(
            ProgramExtension.builder()
                .programmeId("f47ac10b-58cc-4372-a567-0e02b1")
                .extraFinancingDays(30)
                .build(),
            ProgramExtension.builder()
                .programmeId("f47ac10b-58cc-4372-a567-0e02b2")
                .extraFinancingDays(30)
                .build(),
            ProgramExtension.builder()
                .programmeId("f47ac10b-58cc-4372-a567-0e02b3")
                .extraFinancingDays(30)
                .build());
    programExtensionRepository.saveAll(extensions);
    testedProgramRepository.saveAll(programs);

    var programsPage =
        testedProgramRepository.findAllByCustomerMnemonic("SUPER", PageRequest.of(0, 5));

    var expectedContentSize = 2;
    assertEquals(expectedContentSize, programsPage.getContent().size());
  }

  @Test
  void findAllByCustomerMnemonic_itShouldHaveEmptyContentsIfNoProgramsAreFound() {
    Page<Program> programsPage =
        testedProgramRepository.findAllByCustomerMnemonic("SUPER", PageRequest.of(0, 5));

    int expectedContentSize = 0;
    assertEquals(expectedContentSize, programsPage.getContent().size());
  }
}
