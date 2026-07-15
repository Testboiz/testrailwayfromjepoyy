package com.indivaragroup.jdt17wms.dto.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Builder
public class RiskProfilerDTO {
    @NotEmpty(message = "Invalid JSON Body")
    @Valid
    private List<Answer> answers;
    @JsonCreator
    public RiskProfilerDTO(List<Answer> answers) {
        this.answers = answers;
    }

    @JsonValue
    public List<Answer> getAnswers() {
        return answers;
    }

}
