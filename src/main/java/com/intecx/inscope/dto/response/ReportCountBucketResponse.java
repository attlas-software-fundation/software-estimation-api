package com.intecx.inscope.dto.response;

import com.intecx.inscope.enumeration.ProjectStatus;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record ReportCountBucketResponse(
        ProjectStatus status,
        long count,
        BigDecimal share
) {
}
