package com.tcmp.tiapi.program;

import com.tcmp.tiapi.messaging.model.TIOperation;
import com.tcmp.tiapi.messaging.model.TIService;
import com.tcmp.tiapi.messaging.model.requests.ServiceRequest;
import com.tcmp.tiapi.program.messaging.SCFProgrammeMessage;
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
        testedProgramService = new ProgramService(programRepository, producerTemplate);
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

    @Test
    void itShouldWrapProgrammeMessageAsServiceRequest() {
        // Given
        String expectedInvokedRoute = ProgramRouter.DIRECT_CREATE_PROGRAM;
        String expectedProgrammeId = "PROG123";
        String expectedService = TIService.TRADE_INNOVATION.getValue();
        String expectedOperation = TIOperation.SCF_PROGRAMME.getValue();

        SCFProgrammeMessage programmeMessage = SCFProgrammeMessage.builder()
                .id(expectedProgrammeId)
                .build();

        // When
        testedProgramService.sendAndReceiveProgramUUID(programmeMessage);

        // Then
        ArgumentCaptor<String> routeCaptor = ArgumentCaptor.forClass(String.class);
        //  Can't fix this deep generic warning.
        ArgumentCaptor<ServiceRequest<SCFProgrammeMessage>> serviceRequestCaptor =
                ArgumentCaptor.forClass(ServiceRequest.class);

        verify(producerTemplate).requestBody(
                routeCaptor.capture(), serviceRequestCaptor.capture(), any());

        String actualInvokedRoute = routeCaptor.getValue();
        String actualProgrammeId = serviceRequestCaptor.getValue().getBody().getId();
        String actualService = serviceRequestCaptor.getValue().getHeader().getService();
        String actualOperation = serviceRequestCaptor.getValue().getHeader().getOperation();

        assertEquals(expectedInvokedRoute, actualInvokedRoute);
        assertEquals(expectedProgrammeId, actualProgrammeId);
        assertEquals(expectedService, actualService);
        assertEquals(expectedOperation, actualOperation);
    }
}