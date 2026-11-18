package com.intecx.stride.service.impl;

import com.intecx.stride.dto.request.sprint.UpdateSprintConfigRequest;
import com.intecx.stride.dto.response.SprintConfigResponse;
import com.intecx.stride.entity.Project;
import com.intecx.stride.entity.SprintConfig;
import com.intecx.stride.exception.ResourceNotFoundException;
import com.intecx.stride.mapper.SprintConfigMapper;
import com.intecx.stride.repository.ProjectRepository;
import com.intecx.stride.repository.SprintConfigRepository;
import com.intecx.stride.service.SprintConfigService;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SprintConfigServiceImpl implements SprintConfigService {

    private static final String PROJECT_NOT_FOUND = "Proyecto no encontrado";

    private final SprintConfigRepository sprintConfigRepository;
    private final ProjectRepository projectRepository;
    private final SprintConfigMapper sprintConfigMapper;

    @Override
    @Transactional(readOnly = true)
    public SprintConfigResponse getByProject(UUID projectId) {
        projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(PROJECT_NOT_FOUND));
        return sprintConfigRepository.findByProjectId(projectId)
                .map(sprintConfigMapper::toResponse)
                .orElseGet(sprintConfigMapper::defaults);
    }

    @Override
    @Transactional
    public SprintConfigResponse update(UUID projectId, UpdateSprintConfigRequest request, UUID actorId) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(PROJECT_NOT_FOUND));

        SprintConfig config = sprintConfigRepository.findByProjectId(projectId)
                .orElseGet(() -> SprintConfig.builder()
                        .project(project)
                        .build());

        config.setVelocity(request.getVelocity());
        config.setDuration(request.getDuration());
        config.setUnit(request.getUnit());
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy(actorId);

        return sprintConfigMapper.toResponse(sprintConfigRepository.save(config));
    }
}