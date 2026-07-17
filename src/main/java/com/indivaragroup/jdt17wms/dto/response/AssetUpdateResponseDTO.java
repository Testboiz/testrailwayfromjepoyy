package com.indivaragroup.jdt17wms.dto.response;

import com.indivaragroup.jdt17wms.models.enums.TransactionAction;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetUpdateResponseDTO {
    private UUID assetId;
    private UUID transactionId;
    private TransactionAction action;
    private BigDecimal unitsTransacted;
    private BigDecimal amountTransacted;
    private BigDecimal remainingUnits;
    private BigDecimal remainingValue;
    private AssetsPnLResponseDTO pnl;
}
