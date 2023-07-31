package com.tcmp.tiapi.shared.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class PaginatedResult<T> {
    private T data;

    private Map<String, Object> meta;
}
