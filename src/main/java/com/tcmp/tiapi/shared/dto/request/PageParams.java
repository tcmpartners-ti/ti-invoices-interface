package com.tcmp.tiapi.shared.dto.request;

import lombok.Data;

@Data
public class PageParams {
    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_SIZE = 10;

    private int page = DEFAULT_PAGE;
    private int size = DEFAULT_SIZE;
}
