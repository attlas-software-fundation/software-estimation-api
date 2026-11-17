package com.intecx.inscope.service;

import com.intecx.inscope.dto.request.sprint.UpdateSprintConfigRequest;
import com.intecx.inscope.dto.response.SprintConfigResponse;
import java.util.UUID;

public interface SprintConfigService {

    SprintConfigResponse getByProject(UUID projectId);

    SprintConfigResponse update(UUID projectId, UpdateSprintConfigRequest request, UUID actorId);
}