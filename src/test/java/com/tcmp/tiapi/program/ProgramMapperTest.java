package com.tcmp.tiapi.program;

import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.model.Program;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProgramMapperTest {
  private final ProgramMapper programMapper = Mappers.getMapper(ProgramMapper.class);

  @Test
  void itShouldMapEntityToDto() {
    String expectedId = "Program123";
    String expectedCustomerMnemonic = "Customer123";
    Program programMock = Program.builder()
      .pk(1L)
      .id(expectedId)
      .customerMnemonic(expectedCustomerMnemonic)
      .build();

    ProgramDTO programDTO = programMapper.mapEntityToDTO(programMock);

    assertEquals(expectedId, programDTO.getId());
    assertEquals(expectedCustomerMnemonic, programDTO.getCustomer().getMnemonic());
  }
}
