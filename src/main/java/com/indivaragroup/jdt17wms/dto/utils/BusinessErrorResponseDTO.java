package com.indivaragroup.jdt17wms.dto.utils;

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
public class BusinessErrorResponseDTO {
    private String error;
    private String type;
    private Integer code;
}
