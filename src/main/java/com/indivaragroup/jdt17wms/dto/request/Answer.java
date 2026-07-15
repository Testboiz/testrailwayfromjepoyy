package com.indivaragroup.jdt17wms.dto.request;

import com.indivaragroup.jdt17wms.constants.AppConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class Answer {
    private String questionnaireAnswer;

    @NotNull(message = "Invalid JSON Body")
    @Min(value = AppConstants.MIN_ANSWER_SCORE, message = "Invalid JSON Body") // Replace 0 with your AppConstants.MIN_ANSWER_SCORE
    @Max(value = AppConstants.MAX_ANSWER_SCORE, message = "Invalid JSON Body") // Replace 5 with your AppConstants.MAX_ANSWER_SCORE
    private Integer score;
}
