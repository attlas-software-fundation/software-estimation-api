package com.intecx.stride.dto.response;

import com.intecx.stride.enumeration.ProjectType;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record ReportTypeBucketResponse(
        ProjectType type,
        long count,
        BigDecimal share
) {
}
