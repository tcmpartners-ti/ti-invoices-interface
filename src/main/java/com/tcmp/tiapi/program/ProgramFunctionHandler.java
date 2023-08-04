package com.tcmp.tiapi.program;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class ProgramFunctionHandler {
    private final ObjectMapper objectMapper;

    private final Function<String, ProgramDTO> getProgramByIdFunction;

    @FunctionName("getProgramById")
    public HttpResponseMessage getProgramById(
        @HttpTrigger(
            authLevel = AuthorizationLevel.FUNCTION,
            name = "getProgramById",
            methods = {HttpMethod.GET},
            route = "programs/{programId}"
        ) HttpRequestMessage<Void> request,
        @BindingName("programId") String programId
    ) throws JsonProcessingException {
        try {
            ProgramDTO programDTO = getProgramByIdFunction.apply(programId);

            return request.createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(objectMapper.writeValueAsString(programDTO))
                .build();
        } catch (HttpClientErrorException httpException) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/json")
                .body(Map.of("error", Optional
                    .ofNullable(httpException.getMessage())
                    .orElse("Something went wrong.")))
                .build();
        }
    }
}
