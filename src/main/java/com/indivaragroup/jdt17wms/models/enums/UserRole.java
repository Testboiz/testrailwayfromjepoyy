package com.indivaragroup.jdt17wms.models.enums;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum UserRole {
    user,
    admin;

//  @Converter(autoApply = true)
//  public static class UserRoleConverter implements AttributeConverter<UserRole, String> {
//
//    @Override
//    public String convertToDatabaseColumn(UserRole attribute) {
//      return attribute != null ? attribute.name().toLowerCase() : null;
//    }
//
//    @Override
//    public UserRole convertToEntityAttribute(String dbData) {
//      return dbData != null ? UserRole.valueOf(dbData.toUpperCase()) : null;
//    }
//  }
}
