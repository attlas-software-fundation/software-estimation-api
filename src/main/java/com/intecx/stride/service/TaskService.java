package com.intecx.stride.service;

import com.intecx.stride.dto.request.task.CreateTaskRequest;
import com.intecx.stride.dto.request.task.UpdateTaskRequest;
import com.intecx.stride.dto.response.TaskResponse;
import com.intecx.stride.enumeration.TaskStatus;
import java.util.List;
import java.util.UUID;

public interface TaskService {

    List<TaskResponse> listByStory(UUID storyId, String search, TaskStatus status);

    List<TaskResponse> listByProject(UUID projectId, String search, TaskStatus status);

    TaskResponse findById(UUID id);

    TaskResponse create(UUID storyId, CreateTaskRequest request, UUID actorId);

    TaskResponse update(UUID id, UpdateTaskRequest request, UUID actorId);

    void reorder(UUID storyId, List<UUID> taskIds, UUID actorId);

    void delete(UUID id, UUID actorId);
}
