package com.intecx.stride.service.impl;

import com.intecx.stride.dto.response.CostAmountResponse;
import com.intecx.stride.dto.response.ProjectCostResponse;
import com.intecx.stride.dto.response.TaskCostResponse;
import com.intecx.stride.dto.response.TaskProfileCostResponse;
import com.intecx.stride.exception.ConflictException;
import com.intecx.stride.exception.ResourceNotFoundException;
import com.intecx.stride.mapper.ProjectCostMapper;
import com.intecx.stride.repository.ProjectCostProjection;
import com.intecx.stride.repository.ProjectCostRepository;
import com.intecx.stride.repository.ProjectRepository;
import com.intecx.stride.service.ProjectCostService;
import com.intecx.stride.service.support.CostAggregator;
import com.intecx.stride.service.support.CostCalculator;
import com.intecx.stride.service.support.ProfileCostAggregator;
import com.intecx.stride.service.support.TaskCostHoursResolver;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectCostServiceImpl implements ProjectCostService {

    private final ProjectRepository projectRepository;
    private final ProjectCostRepository projectCostRepository;
    private final CostCalculator costCalculator;
    private final CostAggregator costAggregator;
    private final ProfileCostAggregator profileCostAggregator;
    private final ProjectCostMapper projectCostMapper;
    private final TaskCostHoursResolver taskCostHoursResolver;

    @Override
    @Transactional(readOnly = true, isolation = Isolation.REPEATABLE_READ)
    public ProjectCostResponse getCosts(UUID projectId) {
        projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));
        Map<UUID, List<ProjectCostProjection>> rowsByTask = projectCostRepository.findContributions(projectId).stream()
                .collect(Collectors.groupingBy(ProjectCostProjection::getTaskId, LinkedHashMap::new, Collectors.toList()));
        List<TaskCostResponse> tasks = rowsByTask.values().stream()
                .map(this::calculateTaskCost)
                .toList();
        if (tasks.isEmpty()) {
            return projectCostMapper.toResponse(projectId,
                    new CostAmountResponse(BigDecimal.ZERO, null), List.of(), List.of());
        }
        CostAmountResponse total = costAggregator.subtotal(tasks.stream().map(TaskCostResponse::total).toList());
        List<TaskProfileCostResponse> contributions = tasks.stream()
                .flatMap(task -> task.contributions().stream())
                .toList();
        return projectCostMapper.toResponse(projectId, total, tasks, profileCostAggregator.aggregate(contributions));
    }

    private TaskCostResponse calculateTaskCost(List<ProjectCostProjection> rows) {
        Map<UUID, BigDecimal> hours = taskCostHoursResolver.resolve(rows);
        Map<UUID, ProjectCostProjection> rowsByProfile = rows.stream()
                .collect(Collectors.toMap(ProjectCostProjection::getTechnicalProfileId, row -> row));
        List<TaskProfileCostResponse> contributions = hours.entrySet().stream()
                .map(entry -> calculateContribution(rowsByProfile.get(entry.getKey()), entry.getValue()))
                .toList();
        CostAmountResponse total = costAggregator.subtotal(
                contributions.stream().map(TaskProfileCostResponse::cost).toList());
        return projectCostMapper.toTaskResponse(
                rows.getFirst().getTaskId(), rows.getFirst().getTaskTitle(), total, contributions);
    }

    private TaskProfileCostResponse calculateContribution(ProjectCostProjection row, BigDecimal hours) {
        if (!Boolean.TRUE.equals(row.getProfileActive()) || row.getProfileDeletedAt() != null) {
            throw new ConflictException("La tarea %s tiene un perfil inactivo o eliminado".formatted(row.getTaskId()));
        }
        return projectCostMapper.toContributionResponse(row, hours, costCalculator.calculate(hours, row.getHourlyRate()));
    }
}
