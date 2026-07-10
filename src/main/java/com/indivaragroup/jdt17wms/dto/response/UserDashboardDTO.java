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
public class UserDashboardDTO {
    @JsonProperty("portofolio")
    private PortfolioDTO portofolio;

    private List<PerformanceDTO> performance;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PortfolioDTO {
        private String value;
        private String invested;
        private Integer holdings;
        private List<PortfolioItemDTO> items;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PortfolioItemDTO {
        private String name;
        private BigDecimal value;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PerformanceDTO {
        private Integer month;
        private BigDecimal value;
    }
}
