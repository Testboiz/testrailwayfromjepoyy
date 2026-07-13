package com.indivaragroup.jdt17wms.models.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole {
    USER,
    ADMIN;

    @JsonValue
    public String getValue() {
        return name().toLowerCase();
    }
}
