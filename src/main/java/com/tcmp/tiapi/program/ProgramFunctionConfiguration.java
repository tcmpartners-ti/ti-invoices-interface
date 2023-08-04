package com.tcmp.tiapi.program;

import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.model.Program;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
@RequiredArgsConstructor
public class ProgramFunctionConfiguration {
    private final ProgramMapper programMapper;
    private final ProgramService programService;

    @Bean
    public Function<String, ProgramDTO> getProgramByIdFunction() {
        return programId -> {
            Program program = programService.getProgramById(programId);
            return programMapper.mapEntityToDTO(program);
        };
    }
}
