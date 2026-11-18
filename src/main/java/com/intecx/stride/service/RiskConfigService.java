package com.intecx.stride.service;

import com.intecx.stride.dto.request.risk.UpdateRiskConfigRequest;
import com.intecx.stride.dto.response.RiskConfigResponse;
import java.util.UUID;

public interface RiskConfigService {

    RiskConfigResponse getByProject(UUID projectId);

    RiskConfigResponse update(UUID projectId, UpdateRiskConfigRequest request, UUID actorId);
}
