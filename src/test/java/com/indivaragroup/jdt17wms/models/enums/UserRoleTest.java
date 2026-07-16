package com.indivaragroup.jdt17wms.models.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserRoleTest {

    private final UserRoleConverter converter = new UserRoleConverter();

    @Test
    void testUserRoleEnumValues() {
        assertEquals("USER", UserRole.USER.toString());
        assertEquals("ADMIN", UserRole.ADMIN.toString());
        
        // Assert valueOf and values behaviour
        assertEquals(UserRole.USER, UserRole.valueOf("USER"));
        assertEquals(UserRole.ADMIN, UserRole.valueOf("ADMIN"));
        assertEquals(2, UserRole.values().length);
    }

    @Test
    void testConverterConvertToDatabaseColumn() {
        assertNull(converter.convertToDatabaseColumn(null));
        assertEquals("user", converter.convertToDatabaseColumn(UserRole.USER));
        assertEquals("admin", converter.convertToDatabaseColumn(UserRole.ADMIN));
    }

    @Test
    void testConverterConvertToEntityAttribute() {
        assertNull(converter.convertToEntityAttribute(null));
        assertEquals(UserRole.USER, converter.convertToEntityAttribute("user"));
        assertEquals(UserRole.USER, converter.convertToEntityAttribute("USER"));
        assertEquals(UserRole.ADMIN, converter.convertToEntityAttribute("admin"));
        assertEquals(UserRole.ADMIN, converter.convertToEntityAttribute("ADMIN"));
    }

    @Test
    void testConverterConvertToEntityAttributeInvalid_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertToEntityAttribute("invalid_role"));
    }
}
