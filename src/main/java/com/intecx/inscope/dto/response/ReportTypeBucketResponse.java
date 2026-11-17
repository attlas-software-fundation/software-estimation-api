package com.intecx.inscope.dto.response;

import com.intecx.inscope.enumeration.ProjectType;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record ReportTypeBucketResponse(
        ProjectType type,
        long count,
        BigDecimal share
) {
}
