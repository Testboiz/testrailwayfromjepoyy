package com.indivaragroup.jdt17wms.models.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TypeLabels {
    MONEY_MARKET("Money Market"),
    DEPOSIT ("Deposit"),
    BALANCED_FUND("Balanced Fund"),
    MUTUAL_FUND("Mutual Fund"),
    BOND("Bond"),
    SUKUK("Sukuk"),
    STOCK ("Stock");

    private final String value;
}
