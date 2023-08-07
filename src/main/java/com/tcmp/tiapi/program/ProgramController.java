package com.tcmp.tiapi.program;

import com.tcmp.tiapi.program.dto.request.ProgramCreationDTO;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.messaging.SCFProgrammeMessage;
import com.tcmp.tiapi.program.model.Program;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("programs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Programs [WIP]", description = "Defines the programs operations.")
public class ProgramController {
  private final ProgramMapper programMapper;
  private final ProgramService programService;

  @GetMapping("{programId}")
  public ResponseEntity<ProgramDTO> getProgramById(@PathVariable String programId) {
    Program program = programService.getProgramById(programId);
    ProgramDTO programDTO = programMapper.mapEntityToDTO(program);

    return ResponseEntity.ok(programDTO);
  }

  @PostMapping
  public ResponseEntity<ProgramDTO> createProgram(@Valid @RequestBody ProgramCreationDTO programCreationDTO) {
    SCFProgrammeMessage createProgramMessage = programMapper.mapDTOToFTIMessage(programCreationDTO);
    String createdProgramUuid = programService.sendAndReceiveProgramUUID(createProgramMessage);

    return ResponseEntity.ok(ProgramDTO.builder().id(createdProgramUuid).build());
  }
}
