package com.tcmp.tiapi.titoapigee.security;

import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import java.util.Map;

public interface HeaderSigner {

  Map<String, String> buildRequestHeaders(ApiGeeBaseRequest<?> baseRequest);
}
