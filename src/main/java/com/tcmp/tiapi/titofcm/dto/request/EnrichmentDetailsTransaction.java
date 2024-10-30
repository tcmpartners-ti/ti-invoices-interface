package com.tcmp.tiapi.titofcm.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EnrichmentDetailsTransaction {
    private List<MultiSet> multiSet;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class MultiSet{

        @JsonProperty("DEBDESC")
        private String debitDescription;

        @JsonProperty("CREDESC")
        private String creditDescription;

        @JsonProperty("DEBTORACC")
        private String debtorAccount;

    }
}


