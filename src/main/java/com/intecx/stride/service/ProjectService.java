package com.intecx.stride.service;

import com.intecx.stride.common.PaginatedResponse;
import com.intecx.stride.common.QueryRequest;
import com.intecx.stride.dto.request.project.CreateProjectRequest;
import com.intecx.stride.dto.request.project.UpdateProjectRequest;
import com.intecx.stride.dto.request.project.UpdateProjectStatusRequest;
import com.intecx.stride.dto.response.ProjectResponse;
import com.intecx.stride.dto.response.SystemReportSummaryResponse;
import com.intecx.stride.dto.response.ProjectStatusHistoryResponse;
import java.util.List;
import java.util.UUID;

public interface ProjectService {

    PaginatedResponse<ProjectResponse> search(QueryRequest query);

    SystemReportSummaryResponse summarize(QueryRequest query);

    ProjectResponse getById(UUID id);

    ProjectResponse create(CreateProjectRequest request, UUID actorId);

    ProjectResponse update(UUID id, UpdateProjectRequest request, UUID actorId);

    ProjectResponse updateStatus(UUID id, UpdateProjectStatusRequest request, UUID actorId);

    List<ProjectStatusHistoryResponse> getStatusHistory(UUID id);

    void delete(UUID id, UUID actorId);
}
