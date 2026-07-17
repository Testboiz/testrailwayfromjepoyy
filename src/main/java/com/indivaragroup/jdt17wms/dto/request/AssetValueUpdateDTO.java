package com.indivaragroup.jdt17wms.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssetValueUpdateDTO {

    @NotNull(message = "Current value is required")
    @DecimalMin(value = "0.0", inclusive = false)
    @JsonProperty("current_value")
    private BigDecimal currentValue;

    private String notes;
}
