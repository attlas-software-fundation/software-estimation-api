package com.intecx.stride.mapper;

import com.intecx.stride.dto.response.RiskConfigResponse;
import com.intecx.stride.entity.RiskConfig;
import org.springframework.stereotype.Component;

@Component
public class RiskConfigMapper {

    public RiskConfigResponse toResponse(RiskConfig entity) {
        if (entity == null) {
            return null;
        }
        return RiskConfigResponse.builder()
                .level(entity.getLevel())
                .contingencyMargin(entity.getContingencyMargin())
                .saved(true)
                .build();
    }
}
