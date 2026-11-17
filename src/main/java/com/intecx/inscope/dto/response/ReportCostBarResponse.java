package com.intecx.inscope.dto.response;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record ReportCostBarResponse(
        String projectName,
        BigDecimal totalCost,
        BigDecimal share
) {
}
