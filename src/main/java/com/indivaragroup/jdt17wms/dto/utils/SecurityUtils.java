package com.indivaragroup.jdt17wms.dto.utils;

import com.indivaragroup.jdt17wms.constants.AppConstants;
import com.indivaragroup.jdt17wms.dto.response.UserDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.UUID;

public final class SecurityUtils {
    private SecurityUtils() {
        // Prevent instantiation
    }

    public static UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserDTO) {
            return ((UserDTO) auth.getPrincipal()).getId();
        }
        return AppConstants.USER_ID;
    }
}
