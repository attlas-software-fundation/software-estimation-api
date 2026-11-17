package com.intecx.inscope.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;

@Builder
public record DashboardSummaryResponse(
        Long projectCount,
        Long projectsWithEstimation,
        Integer totalStoryPoints,
        BigDecimal totalTimeDays,
        BigDecimal totalCost,
        List<ReportCountBucketResponse> projectsByStatus,
        List<ReportTypeBucketResponse> projectsByType,
        List<ReportCostBarResponse> costByProject
) {
}
