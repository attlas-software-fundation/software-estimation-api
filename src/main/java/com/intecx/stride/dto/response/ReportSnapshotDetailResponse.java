package com.intecx.stride.dto.response;

import com.intecx.stride.enumeration.ProjectStatus;
import com.intecx.stride.enumeration.ProjectType;
import com.intecx.stride.enumeration.RiskLevel;
import com.intecx.stride.enumeration.SprintUnit;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record ReportSnapshotDetailResponse(
        UUID id,
        UUID projectId,
        String projectName,
        ProjectStatus projectStatus,
        ProjectType projectType,
        String projectOwner,
        Integer baseEffortPoints,
        Integer totalSprints,
        BigDecimal totalEffortHours,
        Integer storiesTotal,
        Integer storiesWithPoints,
        Integer tasksTotal,
        Integer tasksWithHours,
        BigDecimal baseTime,
        SprintUnit timeUnit,
        BigDecimal baseCost,
        RiskLevel riskLevel,
        BigDecimal contingencyMarginPercentage,
        BigDecimal contingencyTime,
        BigDecimal contingencyCost,
        BigDecimal totalTime,
        BigDecimal totalCost,
        UUID createdBy,
        LocalDateTime createdAt,
        FileAttachmentResponse file
) {
}
