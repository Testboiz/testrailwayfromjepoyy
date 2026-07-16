package com.indivaragroup.jdt17wms.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskProfilerDTO {
    @NotEmpty(message = "Invalid JSON Body")
    @Valid
    private List<Answer> answers;
}
