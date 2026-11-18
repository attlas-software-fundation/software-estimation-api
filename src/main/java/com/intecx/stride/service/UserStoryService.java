package com.intecx.stride.service;

import com.intecx.stride.dto.request.userstory.CreateUserStoryRequest;
import com.intecx.stride.dto.request.userstory.UpdateUserStoryRequest;
import com.intecx.stride.dto.response.UserStoryResponse;
import com.intecx.stride.enumeration.StoryPriority;
import com.intecx.stride.enumeration.StoryStatus;
import java.util.List;
import java.util.UUID;

public interface UserStoryService {

    List<UserStoryResponse> listByProject(UUID projectId, String search, StoryPriority priority, StoryStatus status);

    UserStoryResponse findById(UUID id);

    UserStoryResponse create(UUID projectId, CreateUserStoryRequest request, UUID actorId);

    UserStoryResponse update(UUID id, UpdateUserStoryRequest request, UUID actorId);

    void reorder(UUID projectId, List<UUID> storyIds, UUID actorId);

    void delete(UUID id, UUID actorId);
}
