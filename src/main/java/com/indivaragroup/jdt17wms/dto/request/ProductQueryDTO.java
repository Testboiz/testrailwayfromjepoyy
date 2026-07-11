package com.indivaragroup.jdt17wms.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductQueryDTO {
    private String searchQuery;
    private String type;
    private Boolean showAll;
    private Boolean dashboardSummary;
}
