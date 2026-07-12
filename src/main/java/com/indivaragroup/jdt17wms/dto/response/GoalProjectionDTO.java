package com.indivaragroup.jdt17wms.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.indivaragroup.jdt17wms.models.enums.GoalStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalProjectionDTO {
    private UUID id;

    private String name;

    private String type;

    @JsonProperty("target_amount")
    private BigDecimal targetAmount;

    @JsonProperty("target_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetDate;

    @JsonProperty("is_priority")
    private Boolean isPriority;

    private String notes;

    private GoalStatus status;

    @JsonProperty("projected-date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate projectedDate;

    @JsonProperty("recommended-contribution")
    private BigDecimal recommendedContribution;

    @JsonProperty("time-series")
    private List<TimeSeriesPointDTO> timeSeries;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TimeSeriesPointDTO {
        private int month;
        private BigDecimal value;
    }
}
