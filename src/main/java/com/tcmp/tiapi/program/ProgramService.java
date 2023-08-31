package com.tcmp.tiapi.program;

import com.tcmp.tiapi.program.messaging.SCFProgrammeMessage;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import lombok.RequiredArgsConstructor;
import org.apache.camel.ProducerTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProgramService {
  private final ProgramRepository programRepository;
  private final ProducerTemplate producerTemplate;
  private final ProgramConfiguration programConfiguration;

  public Program getProgramById(String programId) {
    return programRepository.findById(programId)
      .orElseThrow(() -> new NotFoundHttpException(
        String.format("Could not find a program with id %s.", programId)));
  }

  public void sendProgramToBeCreated(SCFProgrammeMessage programmeMessage) {
    producerTemplate.requestBody(
      programConfiguration.getUriCreateFrom(),
      programmeMessage,
      String.class
    );
  }
}
