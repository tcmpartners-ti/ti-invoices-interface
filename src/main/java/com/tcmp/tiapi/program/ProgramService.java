package com.tcmp.tiapi.program;

import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProgramService {
  private final ProgramRepository programRepository;
  private final ProgramMapper programMapper;

  public ProgramDTO getProgramById(String programId) {
    Program program = programRepository.findById(programId)
      .orElseThrow(() -> new NotFoundHttpException(
        String.format("Could not find a program with id %s.", programId)));

    return programMapper.mapEntityToDTO(program);
  }
}
