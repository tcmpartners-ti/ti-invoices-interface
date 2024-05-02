package com.tcmp.tiapi.shared.dto.response.error;

import java.util.List;

public record CSVErrorsDetails(int status, String message, List<String> errors) {}
