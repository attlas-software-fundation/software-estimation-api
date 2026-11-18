package com.intecx.stride.dto.response;

import java.util.UUID;
import lombok.Builder;

@Builder
public record RoleResponse(
        UUID id,
        String code,
        String name,
        String description,
        boolean isSystem,
        boolean isActive
) {
}
