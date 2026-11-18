package com.intecx.stride.service.impl;

import com.intecx.stride.dto.request.userstory.CreateUserStoryRequest;
import com.intecx.stride.dto.request.userstory.UpdateUserStoryRequest;
import com.intecx.stride.dto.response.UserStoryResponse;
import com.intecx.stride.entity.Project;
import com.intecx.stride.entity.Task;
import com.intecx.stride.entity.UserStory;
import com.intecx.stride.enumeration.StoryPriority;
import com.intecx.stride.enumeration.StoryStatus;
import com.intecx.stride.exception.ConflictException;
import com.intecx.stride.exception.ResourceNotFoundException;
import com.intecx.stride.mapper.UserStoryMapper;
import com.intecx.stride.repository.ProjectRepository;
import com.intecx.stride.repository.TaskRepository;
import com.intecx.stride.repository.UserStoryRepository;
import com.intecx.stride.service.UserStoryService;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserStoryServiceImpl implements UserStoryService {

    private final UserStoryRepository userStoryRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserStoryMapper userStoryMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserStoryResponse> listByProject(
            UUID projectId,
            String search,
            StoryPriority priority,
            StoryStatus status
    ) {
        if (projectRepository.findByIdAndDeletedAtIsNull(projectId).isEmpty()) {
            throw new ResourceNotFoundException("Proyecto no encontrado");
        }

        Specification<UserStory> specification = (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(builder.equal(root.get("project").get("id"), projectId));
            predicates.add(builder.isNull(root.get("deletedAt")));

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(builder.or(
                        builder.like(builder.lower(root.get("code")), pattern),
                        builder.like(builder.lower(root.get("title")), pattern),
                        builder.like(builder.lower(root.get("description")), pattern)
                ));
            }
            if (priority != null) {
                predicates.add(builder.equal(root.get("priority"), priority));
            }
            if (status != null) {
                predicates.add(builder.equal(root.get("status"), status));
            }
            return builder.and(predicates.toArray(Predicate[]::new));
        };

        return userStoryRepository.findAll(specification, Sort.by(Sort.Direction.ASC, "position")).stream()
                .map(userStoryMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public UserStoryResponse findById(UUID id) {
        return userStoryMapper.toResponse(requireActiveStory(id));
    }

    private static final Pattern STORY_CODE_PATTERN = Pattern.compile("^HU-(\\d+)$", Pattern.CASE_INSENSITIVE);

    @Override
    @Transactional
    public UserStoryResponse create(UUID projectId, CreateUserStoryRequest request, UUID actorId) {
        Project project = requireActiveProject(projectId);
        String code;
        if (request.getCode() == null || request.getCode().isBlank()) {
            code = generateStoryCode(projectId);
        } else {
            code = request.getCode().trim().toUpperCase();
            if (userStoryRepository.existsByProject_IdAndCode(projectId, code)) {
                throw new ConflictException("Ya existe una historia con ese código en el proyecto");
            }
        }
        request.setCode(code);

        int position = userStoryRepository.findLastPositionByProjectId(projectId) + 1;
        UserStory story = userStoryMapper.toEntity(request, project, position, actorId);
        return userStoryMapper.toResponse(userStoryRepository.save(story));
    }

    @Override
    @Transactional
    public UserStoryResponse update(UUID id, UpdateUserStoryRequest request, UUID actorId) {
        UserStory story = requireActiveStory(id);
        String code = request.getCode() != null && !request.getCode().isBlank()
                ? request.getCode().trim().toUpperCase()
                : story.getCode();
        request.setCode(code);

        if (!story.getCode().equalsIgnoreCase(code)
                && userStoryRepository.existsByProject_IdAndCodeAndIdNot(story.getProject().getId(), code, id)) {
            throw new ConflictException("Ya existe una historia con ese código en el proyecto");
        }

        userStoryMapper.updateEntity(story, request, actorId);
        return userStoryMapper.toResponse(userStoryRepository.save(story));
    }

    private String generateStoryCode(UUID projectId) {
        List<String> existingCodes = userStoryRepository.findCodesByProjectId(projectId);
        int max = 0;
        for (String c : existingCodes) {
            if (c != null) {
                Matcher matcher = STORY_CODE_PATTERN.matcher(c.trim());
                if (matcher.matches()) {
                    try {
                        int num = Integer.parseInt(matcher.group(1));
                        if (num > max) {
                            max = num;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        int nextNum = max + 1;
        String generated = String.format("HU-%03d", nextNum);
        while (userStoryRepository.existsByProject_IdAndCode(projectId, generated)) {
            nextNum++;
            generated = String.format("HU-%03d", nextNum);
        }
        return generated;
    }

    @Override
    @Transactional
    public void reorder(UUID projectId, List<UUID> storyIds, UUID actorId) {
        requireActiveProject(projectId);
        if (storyIds == null || storyIds.isEmpty()) {
            return;
        }
        List<UserStory> stories = userStoryRepository.findByProject_IdAndDeletedAtIsNull(projectId);
        Map<UUID, UserStory> storyMap = stories.stream()
                .collect(Collectors.toMap(UserStory::getId, s -> s));

        for (int i = 0; i < storyIds.size(); i++) {
            UUID id = storyIds.get(i);
            UserStory story = storyMap.get(id);
            if (story != null) {
                story.setPosition(i + 1);
                story.setUpdatedBy(actorId);
            }
        }
        userStoryRepository.saveAll(stories);
    }

    @Override
    @Transactional
    public void delete(UUID id, UUID actorId) {
        UserStory story = requireActiveStory(id);
        List<Task> tasks = taskRepository.findByStory_Id(id);
        if (!tasks.isEmpty()) {
            taskRepository.deleteAssignmentsByTaskIds(tasks.stream().map(Task::getId).toList());
            taskRepository.deleteAllInBatch(tasks);
        }

        userStoryRepository.delete(story);
    }

    private UserStory requireActiveStory(UUID id) {
        return userStoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Historia de usuario no encontrada"));
    }

    private Project requireActiveProject(UUID id) {
        return projectRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Proyecto no encontrado"));
    }
}