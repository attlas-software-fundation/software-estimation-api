package com.intecx.stride.dto.response;

import com.intecx.stride.enumeration.RiskLevel;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record RiskConfigResponse(
        RiskLevel level,
        BigDecimal contingencyMargin,
        boolean saved
) {
}
