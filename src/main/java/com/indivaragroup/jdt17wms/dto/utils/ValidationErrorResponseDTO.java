package com.indivaragroup.jdt17wms.dto.utils;

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
public class ValidationErrorResponseDTO {
    private String error;
    private String type;
    private Integer code;
    private List<ValidationErrorDetailDTO> details;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValidationErrorDetailDTO {
        private String field;
        private String reason;
    }
}
