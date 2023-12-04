package com.tcmp.tiapi.titoapigee.corporateloan;

import com.tcmp.tiapi.titoapigee.config.RequestBodyEncryptionConfiguration;
import com.tcmp.tiapi.titoapigee.config.ResponseBodyDecryptionConfiguration;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.DistributorCreditRequest;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.DistributorCreditResponse;
import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
  contextId = "corporateLoanContextId",
  value = "corporateLoanClient",
  url = "${bp.api-gee.services.corporate-loan.url}",
  configuration = {ResponseBodyDecryptionConfiguration.class, RequestBodyEncryptionConfiguration.class}
)
public interface CorporateLoanClient {
  @PostMapping("/cfs-loans/distributor-credits")
  DistributorCreditResponse createCredit(
    @RequestHeader Map<String, String> headers,
    @RequestBody ApiGeeBaseRequest<DistributorCreditRequest> request
  );
}
