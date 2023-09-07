package com.tcmp.tiapi.program;

import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.model.Program;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class ProgramMapperTest {
  @Autowired ProgramMapper programMapper;

  @Test
  void mapEntityToDTO_itShouldMapEntityToDto() {
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
