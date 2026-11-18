package com.intecx.stride.service;

import com.intecx.stride.common.PaginatedResponse;
import com.intecx.stride.common.QueryRequest;
import com.intecx.stride.dto.request.profile.CreateTechnicalProfileRequest;
import com.intecx.stride.dto.request.profile.ToggleProfileStatusRequest;
import com.intecx.stride.dto.request.profile.UpdateCerRequest;
import com.intecx.stride.dto.request.profile.UpdateTechnicalProfileRequest;
import com.intecx.stride.dto.response.TechnicalProfileResponse;
import java.util.List;
import java.util.UUID;

public interface TechnicalProfileService {

    PaginatedResponse<TechnicalProfileResponse> search(QueryRequest query);

    List<TechnicalProfileResponse> listActive();

    TechnicalProfileResponse getById(UUID id);

    TechnicalProfileResponse create(CreateTechnicalProfileRequest request, UUID actorId);

    TechnicalProfileResponse update(UUID id, UpdateTechnicalProfileRequest request, UUID actorId);

    TechnicalProfileResponse updateCer(UUID id, UpdateCerRequest request, UUID actorId);

    TechnicalProfileResponse changeStatus(UUID id, ToggleProfileStatusRequest request, UUID actorId);

    void delete(UUID id, UUID actorId);
}
