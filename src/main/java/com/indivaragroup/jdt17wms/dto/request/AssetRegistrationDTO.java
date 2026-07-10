package com.indivaragroup.jdt17wms.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetRegistrationDTO {
    @JsonProperty("product_id")
    private UUID productId;

    private BigDecimal units;

    private BigDecimal amount;

    @JsonProperty("current_value")
    private BigDecimal currentValue;

    @JsonProperty("purchase_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime purchaseDate;

    private String platform;

    private String notes;
}
