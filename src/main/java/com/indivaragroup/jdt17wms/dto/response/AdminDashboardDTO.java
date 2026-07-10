package com.indivaragroup.jdt17wms.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminDashboardDTO {
    private BigDecimal aum;

    @JsonProperty("user_count")
    private Long userCount;

    @JsonProperty("product_count")
    private Long productCount;

    @JsonProperty("total_audit_events")
    private Long totalAuditEvents;

    @JsonProperty("risk_profiles")
    private RiskProfilesDTO riskProfiles;

    @JsonProperty("aum_trend")
    private List<AumTrendDTO> aumTrend;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RiskProfilesDTO {
        @JsonProperty("risk_averse")
        private Integer riskAverse;

        private Integer moderate;

        @JsonProperty("risk_taker")
        private Integer riskTaker;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AumTrendDTO {
        private Integer month;
        private BigDecimal value;
    }
}
