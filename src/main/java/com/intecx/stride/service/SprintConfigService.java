package com.intecx.stride.service;

import com.intecx.stride.dto.request.sprint.UpdateSprintConfigRequest;
import com.intecx.stride.dto.response.SprintConfigResponse;
import java.util.UUID;

public interface SprintConfigService {

    SprintConfigResponse getByProject(UUID projectId);

    SprintConfigResponse update(UUID projectId, UpdateSprintConfigRequest request, UUID actorId);
}