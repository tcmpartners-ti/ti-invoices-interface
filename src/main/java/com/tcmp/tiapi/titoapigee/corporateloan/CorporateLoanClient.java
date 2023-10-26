package com.tcmp.tiapi.titoapigee.corporateloan;

import com.tcmp.tiapi.titoapigee.corporateloan.dto.request.DistributorCreditRequest;
import com.tcmp.tiapi.titoapigee.corporateloan.dto.response.DistributorCreditResponse;
import com.tcmp.tiapi.titoapigee.dto.request.ApiGeeBaseRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(
  value = "corporateLoanClient",
  url = "${bp.api-gee.services.corporate-loan.url}",
  configuration = CorporateLoanConfiguration.class
)
public interface CorporateLoanClient {
  @PostMapping("/cfs-loans/distributor-credits")
  DistributorCreditResponse createCredit(
    @RequestHeader Map<String, String> headers,
    @RequestBody ApiGeeBaseRequest<DistributorCreditRequest> request
  );
}
