package com.indivaragroup.jdt17wms.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskProfilesDTO {
    @JsonProperty("risk_averse")
    private Integer riskAverse;

    private Integer moderate;

    @JsonProperty("risk_taker")
    private Integer riskTaker;
}
