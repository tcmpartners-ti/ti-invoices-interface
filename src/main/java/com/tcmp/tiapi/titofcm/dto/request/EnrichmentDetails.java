package com.tcmp.tiapi.titofcm.dto.request;

import java.util.List;
import lombok.Builder;

@Builder
public record EnrichmentDetails(PaymentSet singleSet, List<PaymentSet> multiSet) {}
