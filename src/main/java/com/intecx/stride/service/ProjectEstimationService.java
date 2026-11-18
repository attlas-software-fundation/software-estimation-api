package com.intecx.stride.service;

import com.intecx.stride.dto.request.project.ProjectContingencyRequest;
import com.intecx.stride.dto.response.ProjectContingencyResponse;
import com.intecx.stride.dto.response.ProjectEstimationResponse;
import com.intecx.stride.dto.response.ReportSnapshotDetailResponse;
import java.util.List;
import java.util.UUID;

public interface ProjectEstimationService {

    ReportSnapshotDetailResponse consolidate(UUID projectId);

    ReportSnapshotDetailResponse create(UUID projectId, UUID actorId);

    List<ProjectEstimationResponse> listByProject(UUID projectId);

    ReportSnapshotDetailResponse getById(UUID projectId, UUID estimationId);

    ProjectContingencyResponse calculate(UUID projectId, ProjectContingencyRequest request);
}
