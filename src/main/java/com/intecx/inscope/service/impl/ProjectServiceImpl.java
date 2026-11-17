package com.intecx.inscope.service.impl;

import com.intecx.inscope.common.PaginatedResponse;
import com.intecx.inscope.common.QueryFields;
import com.intecx.inscope.common.QueryRequest;
import com.intecx.inscope.common.QuerySupport;
import com.intecx.inscope.dto.request.project.CreateProjectRequest;
import com.intecx.inscope.dto.request.project.UpdateProjectRequest;
import com.intecx.inscope.dto.request.project.UpdateProjectStatusRequest;
import com.intecx.inscope.dto.response.ProjectEstimationResponse;
import com.intecx.inscope.dto.response.ProjectResponse;
import com.intecx.inscope.dto.response.ReportCostBarResponse;
import com.intecx.inscope.dto.response.ReportTypeBucketResponse;
import com.intecx.inscope.dto.response.ReportCountBucketResponse;
import com.intecx.inscope.dto.response.SystemReportSummaryResponse;
import com.intecx.inscope.dto.response.ProjectStatusHistoryResponse;
import com.intecx.inscope.entity.Project;
import com.intecx.inscope.entity.ProjectStatusHistory;
import com.intecx.inscope.entity.ReportSnapshot;
import com.intecx.inscope.entity.Task;
import com.intecx.inscope.entity.User;
import com.intecx.inscope.entity.UserStory;
import com.intecx.inscope.enumeration.ProjectStatus;
import com.intecx.inscope.enumeration.ProjectType;
import com.intecx.inscope.enumeration.SprintUnit;
import com.intecx.inscope.exception.ConflictException;
import com.intecx.inscope.exception.ResourceNotFoundException;
import com.intecx.inscope.mapper.ProjectMapper;
import com.intecx.inscope.entity.FileAttachment;
import com.intecx.inscope.repository.FileAttachmentRepository;
import com.intecx.inscope.repository.ReportSnapshotRepository;
import com.intecx.inscope.repository.ProjectRepository;
import com.intecx.inscope.repository.ProjectStatusHistoryRepository;
import com.intecx.inscope.repository.TaskRepository;
import com.intecx.inscope.repository.UserStoryRepository;
import com.intecx.inscope.repository.UserRepository;
import com.intecx.inscope.service.BunnyStorageService;
import com.intecx.inscope.service.ProjectService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private static final QueryFields PROJECT_FIELDS = QueryFields.sortingBy("createdAt")
            .filter("search", "name", "description", "type")
            .filter("name", "name")
            .filter("type", "type")
            .filter("status", "status")
            .filter("startDate", "startDate")
            .filter("plannedDate", "plannedDate")
            .filter("responsibleId", "responsible.id")
            .filter("createdAt", "createdAt")
            .sortable("name", "type", "status", "startDate", "plannedDate");

    private final ProjectRepository projectRepository;
    private final ReportSnapshotRepository reportSnapshotRepository;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final ProjectStatusHistoryRepository projectStatusHistoryRepository;
    private final UserStoryRepository userStoryRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ProjectMapper projectMapper;
    private final BunnyStorageService bunnyStorageService;

    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ProjectResponse> search(QueryRequest query) {
        Specification<Project> notDeletedScope = (root, q, cb) -> cb.isNull(root.get("deletedAt"));
        QueryRequest searchQuery = query == null ? new QueryRequest(null, null) : query;
        return QuerySupport.searchWithBatchMapping(
                projectRepository,
                notDeletedScope,
                PROJECT_FIELDS,
                searchQuery,
                this::toResponsesWithEstimations,
                "projectResponse"
        );
    }

    @Override
    @Transactional(readOnly = true)
    public SystemReportSummaryResponse summarize(QueryRequest query) {
        QueryRequest safe = query == null ? new QueryRequest(null, null) : query;
        Specification<Project> notDeletedScope = (root, q, cb) -> cb.isNull(root.get("deletedAt"));
        List<ProjectResponse> projects = toResponsesWithEstimations(projectRepository.findAll(
                QuerySupport.specification(notDeletedScope, PROJECT_FIELDS, safe)
        ));

        int points = 0;
        BigDecimal days = BigDecimal.ZERO;
        BigDecimal cost = BigDecimal.ZERO;
        long withEstimation = 0;
        Map<ProjectStatus, Long> countsByStatus = new EnumMap<>(ProjectStatus.class);
        Map<ProjectType, Long> countsByType = new EnumMap<>(ProjectType.class);
        List<ReportCostBarResponse> costs = new ArrayList<>();
        for (ProjectResponse project : projects) {
            countsByStatus.merge(project.status(), 1L, Long::sum);
            countsByType.merge(project.type(), 1L, Long::sum);
            if (project.estimations() == null || project.estimations().isEmpty()) {
                continue;
            }
            ProjectEstimationResponse latest = project.estimations().getFirst();
            withEstimation++;
            points += latest.baseEffortPoints() == null ? 0 : latest.baseEffortPoints();
            days = days.add(toDays(latest.totalTime(), latest.timeUnit()));
            BigDecimal projectCost = latest.totalCost() == null ? BigDecimal.ZERO : latest.totalCost();
            cost = cost.add(projectCost);
            costs.add(ReportCostBarResponse.builder()
                    .projectName(project.name())
                    .totalCost(projectCost.setScale(2, RoundingMode.HALF_UP))
                    .share(BigDecimal.ZERO)
                    .build());
        }

        long totalProjects = projects.size();
        List<ReportCountBucketResponse> byStatus = countsByStatus.entrySet().stream()
                .map(entry -> ReportCountBucketResponse.builder()
                        .status(entry.getKey())
                        .count(entry.getValue())
                        .share(share(entry.getValue(), totalProjects))
                        .build())
                .toList();
        List<ReportTypeBucketResponse> byType = countsByType.entrySet().stream()
                .map(entry -> ReportTypeBucketResponse.builder()
                        .type(entry.getKey())
                        .count(entry.getValue())
                        .share(share(entry.getValue(), totalProjects))
                        .build())
                .toList();
        BigDecimal maxCost = costs.stream()
                .map(ReportCostBarResponse::totalCost)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ONE);
        List<ReportCostBarResponse> topCosts = costs.stream()
                .sorted(Comparator.comparing(ReportCostBarResponse::totalCost).reversed())
                .limit(8)
                .map(bar -> ReportCostBarResponse.builder()
                        .projectName(bar.projectName())
                        .totalCost(bar.totalCost())
                        .share(share(bar.totalCost(), maxCost))
                        .build())
                .toList();

        return SystemReportSummaryResponse.builder()
                .projectCount(totalProjects)
                .projectsWithEstimation(withEstimation)
                .totalStoryPoints(points)
                .totalTimeDays(days.setScale(2, RoundingMode.HALF_UP))
                .totalCost(cost.setScale(2, RoundingMode.HALF_UP))
                .projectsByStatus(byStatus)
                .projectsByType(byType)
                .costByProject(topCosts)
                .build();
    }

    private BigDecimal share(long part, long total) {
        if (total <= 0) {
            return BigDecimal.ZERO;
        }
        return share(BigDecimal.valueOf(part), BigDecimal.valueOf(total));
    }

    private BigDecimal share(BigDecimal part, BigDecimal total) {
        if (total == null || total.signum() <= 0) {
            return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
        }
        return part.divide(total, 4, RoundingMode.HALF_UP);
    }

    private BigDecimal toDays(BigDecimal time, SprintUnit unit) {
        BigDecimal value = time == null ? BigDecimal.ZERO : time;
        if (unit == SprintUnit.WEEKS) {
            return value.multiply(BigDecimal.valueOf(7));
        }
        return value;
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectResponse getById(UUID id) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));
        return toResponsesWithEstimations(List.of(project)).getFirst();
    }

    @Override
    @Transactional
    public ProjectResponse create(CreateProjectRequest request, UUID actorId) {
        User responsible = userRepository.findActiveById(request.getResponsibleId())
                .orElseThrow(() -> new ResourceNotFoundException("El responsable especificado no existe"));

        Project project = projectMapper.toEntity(request, responsible, actorId);
        Project savedProject = projectRepository.save(project);
        recordStatusChange(savedProject, null, savedProject.getStatus(), actorId);
        return projectMapper.toResponse(savedProject);
    }

    @Override
    @Transactional
    public ProjectResponse update(UUID id, UpdateProjectRequest request, UUID actorId) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));
        if (project.getStatus() != ProjectStatus.DRAFT) {
            throw new ConflictException("Solo se pueden actualizar proyectos en estado Borrador");
        }

        User responsible = userRepository.findActiveById(request.getResponsibleId())
                .orElseThrow(() -> new ResourceNotFoundException("El responsable especificado no existe"));
        projectMapper.updateEntity(project, request, responsible, actorId);
        return projectMapper.toResponse(projectRepository.save(project));
    }

    @Override
    @Transactional
    public ProjectResponse updateStatus(UUID id, UpdateProjectStatusRequest request, UUID actorId) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));

        ProjectStatus currentStatus = project.getStatus();
        ProjectStatus nextStatus = request.getStatus();
        if (!currentStatus.canTransitionTo(nextStatus)) {
            throw new ConflictException(
                    "No se permite la transición del proyecto de %s a %s".formatted(currentStatus, nextStatus)
            );
        }

        project.setStatus(nextStatus);
        project.setUpdatedBy(actorId);
        Project savedProject = projectRepository.save(project);
        recordStatusChange(savedProject, currentStatus, nextStatus, actorId);
        return projectMapper.toResponse(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectStatusHistoryResponse> getStatusHistory(UUID id) {
        projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));
        return projectStatusHistoryRepository.findByProjectIdWithChangedBy(id).stream()
                .map(projectMapper::toStatusHistoryResponse)
                .toList();
    }

    @Override
    @Transactional
    public void delete(UUID id, UUID actorId) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));
        requireDeletableStatus(project.getStatus());

        List<ReportSnapshot> snapshots = reportSnapshotRepository.findByProject_Id(id);
        for (ReportSnapshot snapshot : snapshots) {
            FileAttachment file = snapshot.getFile();
            if (file != null) {
                snapshot.setFile(null);
                reportSnapshotRepository.flush();
                bunnyStorageService.delete(file.getStoragePath());
                fileAttachmentRepository.delete(file);
            }
        }
        if (!snapshots.isEmpty()) {
            reportSnapshotRepository.deleteAllInBatch(snapshots);
        }

        List<Task> tasks = taskRepository.findByStory_Project_Id(id);
        if (!tasks.isEmpty()) {
            taskRepository.deleteAssignmentsByTaskIds(tasks.stream().map(Task::getId).toList());
            taskRepository.deleteAllInBatch(tasks);
        }

        List<UserStory> stories = userStoryRepository.findByProject_Id(id);
        if (!stories.isEmpty()) {
            userStoryRepository.deleteAllInBatch(stories);
        }

        projectStatusHistoryRepository.deleteByProject_Id(id);
        projectRepository.flush();
        projectRepository.delete(project);
    }

    private List<ProjectResponse> toResponsesWithEstimations(List<Project> projects) {
        if (projects.isEmpty()) {
            return List.of();
        }

        List<UUID> projectIds = projects.stream().map(Project::getId).toList();
        return projectMapper.toResponses(
                projects,
                reportSnapshotRepository.findByProjectIds(projectIds)
        );
    }

    private void requireDeletableStatus(ProjectStatus status) {
        if (status != ProjectStatus.DRAFT && status != ProjectStatus.REJECTED) {
            throw new ConflictException(
                    "El proyecto debe estar en estado Borrador o Rechazado para eliminarlo o eliminar sus estimaciones"
            );
        }
    }

    private void recordStatusChange(Project project, ProjectStatus previousStatus, ProjectStatus newStatus, UUID actorId) {
        ProjectStatusHistory history = ProjectStatusHistory.builder()
                .project(project)
                .previousStatus(previousStatus)
                .newStatus(newStatus)
                .changedBy(userRepository.getReferenceById(actorId))
                .changedAt(LocalDateTime.now())
                .build();
        projectStatusHistoryRepository.save(history);
    }
}
