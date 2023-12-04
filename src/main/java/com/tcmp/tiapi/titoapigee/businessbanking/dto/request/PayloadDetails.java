package com.tcmp.tiapi.titoapigee.businessbanking.dto.request;

import java.util.List;

public record PayloadDetails(List<String> errors, List<String> warnings, List<String> infos) {}
