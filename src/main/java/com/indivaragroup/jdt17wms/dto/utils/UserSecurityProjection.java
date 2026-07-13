package com.indivaragroup.jdt17wms.dto.utils;

import com.indivaragroup.jdt17wms.models.enums.UserRole;

public interface UserSecurityProjection {
    String getEmail();
    UserRole getRole();
    Long getPriorCount();
}
