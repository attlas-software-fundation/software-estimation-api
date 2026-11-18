package com.intecx.stride.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;

@Builder
public record SystemReportSummaryResponse(
        long projectCount,
        long projectsWithEstimation,
        int totalStoryPoints,
        BigDecimal totalTimeDays,
        BigDecimal totalCost,
        List<ReportCountBucketResponse> projectsByStatus,
        List<ReportTypeBucketResponse> projectsByType,
        List<ReportCostBarResponse> costByProject
) {
}
