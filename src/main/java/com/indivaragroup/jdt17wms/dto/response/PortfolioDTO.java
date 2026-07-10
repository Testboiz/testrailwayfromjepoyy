package com.indivaragroup.jdt17wms.dto.response;

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
public class PortfolioDTO {
    private String value;
    private String invested;
    private Integer holdings;
    private List<PortfolioItemDTO> items;
}
