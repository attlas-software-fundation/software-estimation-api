package com.intecx.stride.mapper;

import com.intecx.stride.dto.request.project.CreateProjectRequest;
import com.intecx.stride.dto.request.project.UpdateProjectRequest;
import com.intecx.stride.dto.response.ProjectEstimationResponse;
import com.intecx.stride.dto.response.ProjectResponse;
import com.intecx.stride.dto.response.ProjectStatusHistoryResponse;
import com.intecx.stride.entity.Project;
import com.intecx.stride.entity.ProjectStatusHistory;
import com.intecx.stride.entity.ReportSnapshot;
import com.intecx.stride.entity.User;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectMapper {

    private final ReportSnapshotMapper reportSnapshotMapper;

    public ProjectResponse toResponse(Project project) {
        return toResponse(project, List.of());
    }

    public ProjectResponse toResponse(Project project, List<ProjectEstimationResponse> estimations) {
        if (project == null) {
            return null;
        }

        UUID responsibleId = project.getResponsible() != null ? project.getResponsible().getId() : null;

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .type(project.getType())
                .startDate(project.getStartDate())
                .plannedDate(project.getPlannedDate())
                .responsibleId(responsibleId)
                .status(project.getStatus())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt())
                .estimations(estimations)
                .build();
    }

    public List<ProjectResponse> toResponses(List<Project> projects, List<ReportSnapshot> snapshots) {
        Map<UUID, List<ProjectEstimationResponse>> estimationsByProject = snapshots.stream()
                .collect(Collectors.groupingBy(
                        snapshot -> snapshot.getProject().getId(),
                        Collectors.mapping(reportSnapshotMapper::toResponse, Collectors.toList())
                ));
        return projects.stream()
                .map(project -> toResponse(
                        project,
                        estimationsByProject.getOrDefault(project.getId(), List.of())
                ))
                .toList();
    }

    public ProjectStatusHistoryResponse toStatusHistoryResponse(ProjectStatusHistory history) {
        User changedBy = history.getChangedBy();
        return ProjectStatusHistoryResponse.builder()
                .id(history.getId())
                .previousStatus(history.getPreviousStatus())
                .newStatus(history.getNewStatus())
                .changedAt(history.getChangedAt())
                .changedById(changedBy.getId())
                .changedByFirstName(changedBy.getFirstName())
                .changedByLastName(changedBy.getLastName())
                .changedByEmail(changedBy.getEmail())
                .build();
    }

    public Project toEntity(CreateProjectRequest request, User responsible, UUID actorId) {
        if (request == null) {
            return null;
        }

        return Project.builder()
                .name(request.getName().trim())
                .description(request.getDescription() == null ? "" : request.getDescription().trim())
                .type(request.getType())
                .startDate(request.getStartDate())
                .plannedDate(request.getPlannedDate())
                .responsible(responsible)
                .createdBy(actorId)
                .build();
    }

    public void updateEntity(Project project, UpdateProjectRequest request, User responsible, UUID actorId) {
        project.setName(request.getName().trim());
        project.setDescription(request.getDescription() == null ? "" : request.getDescription().trim());
        project.setType(request.getType());
        project.setStartDate(request.getStartDate());
        project.setPlannedDate(request.getPlannedDate());
        project.setResponsible(responsible);
        project.setUpdatedBy(actorId);
    }
}
