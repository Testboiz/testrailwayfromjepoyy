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
public class ComponentDTO {
    @JsonProperty("componentName")
    private String componentName;
    
    private String label;
    private Integer score;
    private Integer maxScore;
}
