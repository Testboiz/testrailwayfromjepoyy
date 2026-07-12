package com.indivaragroup.jdt17wms.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskProfilerResponseDTO {
    private UUID id;

    @JsonProperty("risk_profile")
    private String riskProfile;

    @JsonProperty("questionnaire_completed")
    private Boolean questionnaireCompleted;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    private Integer score;
}
