package com.indivaragroup.jdt17wms.dto.utils;

import com.indivaragroup.jdt17wms.constants.AppConstants;
import com.indivaragroup.jdt17wms.dto.response.UserDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Constructor;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<SecurityUtils> constructor = SecurityUtils.class.getDeclaredConstructor();
        assertTrue(java.lang.reflect.Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        SecurityUtils instance = constructor.newInstance();
        assertNotNull(instance);
    }

    @Test
    void getCurrentUserId_whenAuthenticationIsNull_shouldReturnDefaultUserId() {
        SecurityContextHolder.clearContext();
        assertEquals(AppConstants.USER_ID, SecurityUtils.getCurrentUserId());
    }

    @Test
    void getCurrentUserId_whenPrincipalIsNotUserDTO_shouldReturnDefaultUserId() {
        Authentication auth = new UsernamePasswordAuthenticationToken("anonymous", null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals(AppConstants.USER_ID, SecurityUtils.getCurrentUserId());
    }

    @Test
    void getCurrentUserId_whenPrincipalIsUserDTO_shouldReturnUserId() {
        UUID expectedId = UUID.randomUUID();
        UserDTO userDTO = UserDTO.builder()
                .id(expectedId)
                .email("user@example.com")
                .build();
        Authentication auth = new UsernamePasswordAuthenticationToken(userDTO, null);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertEquals(expectedId, SecurityUtils.getCurrentUserId());
    }
}
