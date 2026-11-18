package com.intecx.stride.mapper;

import com.intecx.stride.dto.response.CostAmountResponse;
import com.intecx.stride.dto.response.ProfileCostResponse;
import com.intecx.stride.dto.response.ProjectCostResponse;
import com.intecx.stride.dto.response.TaskCostResponse;
import com.intecx.stride.dto.response.TaskProfileCostResponse;
import com.intecx.stride.enumeration.CurrencyCode;
import com.intecx.stride.repository.ProjectCostProjection;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ProjectCostMapper {

    public TaskProfileCostResponse toContributionResponse(ProjectCostProjection row, BigDecimal hours, BigDecimal cost) {
        return TaskProfileCostResponse.builder()
                .technicalProfileId(row.getTechnicalProfileId())
                .profileName(row.getProfileName())
                .profileEmail(row.getProfileEmail())
                .profileRole(row.getProfileRole())
                .hours(hours)
                .hourlyRate(row.getHourlyRate())
                .cost(new CostAmountResponse(cost, CurrencyCode.valueOf(row.getCurrency())))
                .build();
    }

    public TaskCostResponse toTaskResponse(UUID taskId, String taskTitle, CostAmountResponse total,
            List<TaskProfileCostResponse> contributions) {
        return TaskCostResponse.builder()
                .taskId(taskId)
                .taskTitle(taskTitle)
                .total(total)
                .contributions(contributions)
                .build();
    }

    public ProjectCostResponse toResponse(UUID projectId, CostAmountResponse total,
            List<TaskCostResponse> tasks, List<ProfileCostResponse> profiles) {
        return ProjectCostResponse.builder()
                .projectId(projectId)
                .total(total)
                .tasks(tasks)
                .profiles(profiles)
                .build();
    }
}
