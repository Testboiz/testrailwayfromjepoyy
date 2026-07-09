package com.indivaragroup.jdt17wms.models.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum UserRole {
    USER,
    ADMIN,
    MODERATOR;

    public String toValue() {
        return this.name().toLowerCase();
    }

    public static UserRole fromValue(String value) {
        if (value == null) {
            return null;
        }
        return UserRole.valueOf(value.toUpperCase());
    }

    @Converter(autoApply = true)
    public static class UserRoleConverter implements AttributeConverter<UserRole, String> {
        @Override
        public String convertToDatabaseColumn(UserRole attribute) {
            return attribute != null ? attribute.toValue() : null;
        }

        @Override
        public UserRole convertToEntityAttribute(String dbData) {
            return dbData != null ? UserRole.fromValue(dbData) : null;
        }
    }
}
