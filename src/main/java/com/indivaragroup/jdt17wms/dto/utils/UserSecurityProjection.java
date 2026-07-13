package com.indivaragroup.jdt17wms.dto.utils;

import com.indivaragroup.jdt17wms.models.enums.UserRole;
import java.util.UUID;

public interface UserSecurityProjection {
    UUID getId();
    String getName();
    String getEmail();
    UserRole getRole();
    Long getPriorCount();
}
