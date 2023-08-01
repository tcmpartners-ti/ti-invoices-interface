package com.tcmp.tiapi.program;

import com.tcmp.tiapi.program.dto.request.ProgramCreationDTO;
import com.tcmp.tiapi.program.dto.response.ProgramDTO;
import com.tcmp.tiapi.program.messaging.SCFProgrammeMessage;
import com.tcmp.tiapi.program.model.Program;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ProgramMapper {
    @Mapping(source = "id", target = "id")
    @Mapping(source = "description", target = "name")
    @Mapping(source = "customerMnemonic", target = "customer.mnemonic")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "expiryDate", target = "endDate")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "availableLimitCurrencyCode", target = "creditLimit.currency")
    @Mapping(source = "availableLimitAmount", target = "creditLimit.amount")
    @Mapping(source = "status", target = "status")
    ProgramDTO mapEntityToDTO(Program program);

    @Mapping(source = "description", target = "description")
    @Mapping(source = "customer.mnemonic", target = "customer.mnemonic")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "type", target = "type")
    @Mapping(source = "creditLimit.amount", target = "creditLimit.amount")
    @Mapping(source = "creditLimit.currency", target = "creditLimit.currency")
    @Mapping(source = "status", target = "status")
    SCFProgrammeMessage mapDTOToFTIMessage(ProgramCreationDTO programCreationDTO);
}
