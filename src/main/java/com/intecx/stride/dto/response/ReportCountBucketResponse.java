package com.intecx.stride.dto.response;

import com.intecx.stride.enumeration.ProjectStatus;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record ReportCountBucketResponse(
        ProjectStatus status,
        long count,
        BigDecimal share
) {
}
