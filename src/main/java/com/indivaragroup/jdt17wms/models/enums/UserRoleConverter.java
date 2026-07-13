package com.indivaragroup.jdt17wms.models.enums;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserRoleConverter implements AttributeConverter<UserRole, Object> {

    @Override
    public Object convertToDatabaseColumn(UserRole attribute) {
        return attribute == null ? null : attribute.name().toLowerCase();
    }

    @Override
    public UserRole convertToEntityAttribute(Object dbData) {
        return dbData == null ? null : UserRole.valueOf(dbData.toString().toUpperCase());
    }
}
