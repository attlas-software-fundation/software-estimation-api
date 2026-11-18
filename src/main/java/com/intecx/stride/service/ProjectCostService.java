package com.intecx.stride.service;

import com.intecx.stride.dto.response.ProjectCostResponse;
import java.util.UUID;

public interface ProjectCostService {

    ProjectCostResponse getCosts(UUID projectId);
}
