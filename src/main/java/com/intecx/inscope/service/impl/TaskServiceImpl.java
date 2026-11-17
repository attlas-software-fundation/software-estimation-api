package com.intecx.inscope.service.impl;

import com.intecx.inscope.dto.request.task.CreateTaskRequest;
import com.intecx.inscope.dto.request.task.UpdateTaskRequest;
import com.intecx.inscope.dto.response.TaskResponse;
import com.intecx.inscope.entity.Task;
import com.intecx.inscope.entity.TechnicalProfile;
import com.intecx.inscope.entity.UserStory;
import com.intecx.inscope.enumeration.TaskStatus;
import com.intecx.inscope.exception.BadRequestException;
import com.intecx.inscope.exception.ResourceNotFoundException;
import com.intecx.inscope.mapper.TaskMapper;
import com.intecx.inscope.repository.ProjectRepository;
import com.intecx.inscope.repository.TaskRepository;
import com.intecx.inscope.repository.TechnicalProfileRepository;
import com.intecx.inscope.repository.UserStoryRepository;
import com.intecx.inscope.service.TaskService;
import jakarta.persistence.criteria.Predicate;
import java.util.*;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserStoryRepository userStoryRepository;
    private final ProjectRepository projectRepository;
    private final TechnicalProfileRepository technicalProfileRepository;
    private final TaskMapper taskMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> listByStory(UUID storyId, String search, TaskStatus status) {
        requireActiveStory(storyId);
        Specification<Task> scope = (root, criteria, builder) -> builder.and(
                builder.equal(root.get("story").get("id"), storyId),
                builder.isNull(root.get("deletedAt")),
                builder.isNull(root.get("story").get("deletedAt"))
        );
        return findFilteredTasks(scope, search, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> listByProject(UUID projectId, String search, TaskStatus status) {
        ensureActiveProject(projectId);
        Specification<Task> scope = (root, criteria, builder) -> builder.and(
                builder.equal(root.get("story").get("project").get("id"), projectId),
                builder.isNull(root.get("deletedAt")),
                builder.isNull(root.get("story").get("deletedAt")),
                builder.isNull(root.get("story").get("project").get("deletedAt"))
        );
        return findFilteredTasks(scope, search, status);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse findById(UUID id) {
        return taskMapper.toResponse(requireActiveTask(id));
    }

    @Override
    @Transactional
    public TaskResponse create(UUID storyId, CreateTaskRequest request, UUID actorId) {
        UserStory story = requireActiveStory(storyId);
        int position = taskRepository.findLastPositionByStoryId(storyId) + 1;
        Task task = taskMapper.toEntity(request, story, position, actorId);
        if (request.getResponsibleProfileIds() != null) {
            task.setResponsibleProfiles(new LinkedHashSet<>(resolveProfiles(request.getResponsibleProfileIds())));
        }
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public TaskResponse update(UUID id, UpdateTaskRequest request, UUID actorId) {
        Task task = requireActiveTask(id);
        taskMapper.updateEntity(task, request, actorId);
        if (request.getResponsibleProfileIds() != null) {
            task.setResponsibleProfiles(new LinkedHashSet<>(resolveProfiles(request.getResponsibleProfileIds())));
        }
        return taskMapper.toResponse(taskRepository.save(task));
    }

    @Override
    @Transactional
    public void reorder(UUID storyId, List<UUID> taskIds, UUID actorId) {
        UserStory story = requireActiveStory(storyId);
        if (taskIds == null || taskIds.isEmpty()) {
            return;
        }
        List<Task> tasks = taskRepository.findByStory_IdAndDeletedAtIsNull(storyId);
        Map<UUID, Task> taskMap = tasks.stream()
                .collect(Collectors.toMap(Task::getId, t -> t));

        for (int i = 0; i < taskIds.size(); i++) {
            UUID id = taskIds.get(i);
            Task task = taskMap.get(id);
            if (task != null) {
                task.setStory(story);
                task.setPosition(i + 1);
                task.setUpdatedBy(actorId);
            }
        }
        taskRepository.saveAll(tasks);
    }

    @Override
    @Transactional
    public void delete(UUID id, UUID actorId) {
        Task task = requireActiveTask(id);
        task.getResponsibleProfiles().clear();
        taskRepository.flush();
        taskRepository.delete(task);
    }

    private List<TaskResponse> findFilteredTasks(Specification<Task> scope, String search, TaskStatus status) {
        Specification<Task> filtered = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(scope.toPredicate(root, query, builder));
            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("title")), pattern),
                        builder.like(builder.lower(root.get("description")), pattern)
                ));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };

        return taskRepository.findAll(filtered, Sort.by(Sort.Direction.ASC, "position")).stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    private List<TechnicalProfile> resolveProfiles(List<UUID> profileIds) {
        if (profileIds.stream().anyMatch(Objects::isNull)) {
            throw new BadRequestException("Los identificadores de responsables no pueden ser nulos");
        }
        Set<UUID> uniqueIds = new LinkedHashSet<>(profileIds);
        if (uniqueIds.size() != profileIds.size()) {
            throw new BadRequestException("La lista contiene perfiles técnicos duplicados");
        }
        if (uniqueIds.isEmpty()) {
            return List.of();
        }

        List<TechnicalProfile> profiles = technicalProfileRepository.findByIdInAndDeletedAtIsNullAndIsActiveTrue(uniqueIds);
        if (profiles.size() != uniqueIds.size()) {
            throw new ResourceNotFoundException("Uno o más perfiles técnicos no existen o están inactivos");
        }
        return profiles;
    }

    private Task requireActiveTask(UUID id) {
        return taskRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tarea no encontrada"));
    }

    private UserStory requireActiveStory(UUID id) {
        UserStory story = userStoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Historia de usuario no encontrada"));
        if (story.getProject().getDeletedAt() != null) {
            throw new ResourceNotFoundException("Historia de usuario no encontrada");
        }
        return story;
    }

    private void ensureActiveProject(UUID id) {
        if (projectRepository.findByIdAndDeletedAtIsNull(id).isEmpty()) {
            throw new ResourceNotFoundException("Proyecto no encontrado");
        }
    }
}