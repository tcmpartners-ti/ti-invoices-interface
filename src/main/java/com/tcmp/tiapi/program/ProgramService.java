package com.tcmp.tiapi.program;

import com.tcmp.tiapi.messaging.TIServiceRequestWrapper;
import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.messaging.model.TIService;
import com.tcmp.tiapi.messaging.model.requests.ServiceRequest;
import com.tcmp.tiapi.program.messaging.SCFProgrammeMessage;
import com.tcmp.tiapi.program.model.Program;
import com.tcmp.tiapi.shared.exception.NotFoundHttpException;
import lombok.RequiredArgsConstructor;
import org.apache.camel.ProducerTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProgramService {
    private final ProgramRepository programRepository;
    private final ProducerTemplate producerTemplate;

    @Value("${route.program.create.single.from}")
    private String uriFrom;

    public Program getProgramById(String programId) {
        return programRepository.findById(programId)
                .orElseThrow(() -> new NotFoundHttpException(
                        String.format("Could not find a program with id %s.", programId)));
    }

    public String sendAndReceiveProgramUUID(SCFProgrammeMessage programmeMessage) {
        ServiceRequest<SCFProgrammeMessage> createProgramRequest =
            TIServiceRequestWrapper.wrapRequest(TIService.TRADE_INNOVATION, TIOperation.SCF_PROGRAMME, programmeMessage);

        return producerTemplate.requestBody(uriFrom, createProgramRequest, String.class);
    }
}
