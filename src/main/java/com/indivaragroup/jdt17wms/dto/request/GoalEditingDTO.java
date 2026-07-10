package com.indivaragroup.jdt17wms.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalEditingDTO {
    private String name;

    @JsonProperty("target_amount")
    private BigDecimal targetAmount;

    @JsonProperty("monthly_contribution")
    private BigDecimal monthlyContribution;

    @JsonProperty("target_date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate targetDate;

    @JsonProperty("is_priority")
    private Boolean isPriority;

    private String notes;
}
