package com.intecx.stride.dto.response;

import com.intecx.stride.enumeration.SprintUnit;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ProjectEstimationResponse(
        UUID id,
        Integer baseEffortPoints,
        BigDecimal totalEffortHours,
        BigDecimal totalTime,
        SprintUnit timeUnit,
        BigDecimal totalCost,
        LocalDateTime createdAt
) {
}
