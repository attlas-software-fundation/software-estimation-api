package com.intecx.inscope.service.impl;

import com.intecx.inscope.dto.request.sprint.UpdateSprintConfigRequest;
import com.intecx.inscope.dto.response.SprintConfigResponse;
import com.intecx.inscope.entity.Project;
import com.intecx.inscope.entity.SprintConfig;
import com.intecx.inscope.exception.ResourceNotFoundException;
import com.intecx.inscope.mapper.SprintConfigMapper;
import com.intecx.inscope.repository.ProjectRepository;
import com.intecx.inscope.repository.SprintConfigRepository;
import com.intecx.inscope.service.SprintConfigService;
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