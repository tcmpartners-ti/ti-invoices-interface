package com.tcmp.tiapi.program;

import com.tcmp.tiapi.program.dto.request.ProgramCreationDTO;
import com.tcmp.tiapi.program.dto.response.ProgramCreatedDTO;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.messaging.SCFProgrammeMessage;
import com.tcmp.tiapi.program.model.Program;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("programs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Programs", description = "Defines the programs operations.")
public class ProgramController {
  private final ProgramService programService;
  private final ProgramMapper programMapper;

  @GetMapping(path = "{programId}", produces = MediaType.APPLICATION_JSON_VALUE)
  @Operation(description = "Get a program by its identifier.")
  public ProgramDTO getProgramById(@PathVariable String programId) {
    Program program = programService.getProgramById(programId);
    return programMapper.mapEntityToDTO(program);
  }

  @PostMapping
  @Operation(description = "Creates a program in Trade Innovation.")
  public ProgramCreatedDTO createProgram(@Valid @RequestBody ProgramCreationDTO programCreationDTO) {
    SCFProgrammeMessage createProgramMessage = programMapper.mapDTOToFTIMessage(programCreationDTO);
    programService.sendProgramToBeCreated(createProgramMessage);

    return ProgramCreatedDTO.builder()
      .message("Program sent to be created.")
      .build();
  }
}
