package com.intecx.inscope.dto.response;

import com.intecx.inscope.enumeration.SprintUnit;
import lombok.Builder;

@Builder
public record SprintConfigResponse(
        Integer velocity,
        Integer duration,
        SprintUnit unit
) {
}