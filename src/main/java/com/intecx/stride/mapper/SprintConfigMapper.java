package com.intecx.stride.mapper;

import com.intecx.stride.dto.response.SprintConfigResponse;
import com.intecx.stride.entity.SprintConfig;
import com.intecx.stride.enumeration.SprintUnit;
import org.springframework.stereotype.Component;

@Component
public class SprintConfigMapper {

    public static final int DEFAULT_VELOCITY = 5;
    public static final int DEFAULT_DURATION = 2;
    public static final SprintUnit DEFAULT_UNIT = SprintUnit.DAYS;

    public SprintConfigResponse toResponse(SprintConfig entity) {
        if (entity == null) {
            return defaults();
        }
        return SprintConfigResponse.builder()
                .velocity(entity.getVelocity())
                .duration(entity.getDuration())
                .unit(entity.getUnit())
                .build();
    }

    public SprintConfigResponse defaults() {
        return SprintConfigResponse.builder()
                .velocity(DEFAULT_VELOCITY)
                .duration(DEFAULT_DURATION)
                .unit(DEFAULT_UNIT)
                .build();
    }
}