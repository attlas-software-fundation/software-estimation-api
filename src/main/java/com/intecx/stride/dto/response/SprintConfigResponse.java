package com.intecx.stride.dto.response;

import com.intecx.stride.enumeration.SprintUnit;
import lombok.Builder;

@Builder
public record SprintConfigResponse(
        Integer velocity,
        Integer duration,
        SprintUnit unit
) {
}