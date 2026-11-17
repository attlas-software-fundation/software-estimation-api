package com.intecx.inscope.service.impl;

import com.intecx.inscope.dto.request.risk.UpdateRiskConfigRequest;
import com.intecx.inscope.dto.response.RiskConfigResponse;
import com.intecx.inscope.enumeration.RiskLevel;
import com.intecx.inscope.entity.Project;
import com.intecx.inscope.entity.RiskConfig;
import com.intecx.inscope.exception.ResourceNotFoundException;
import com.intecx.inscope.mapper.RiskConfigMapper;
import com.intecx.inscope.repository.ProjectRepository;
import com.intecx.inscope.repository.RiskConfigRepository;
import com.intecx.inscope.service.RiskConfigService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RiskConfigServiceImpl implements RiskConfigService {

    private static final String PROJECT_NOT_FOUND = "Proyecto no encontrado";
    private static final BigDecimal DEFAULT_MARGIN = BigDecimal.valueOf(15);

    private final RiskConfigRepository riskConfigRepository;
    private final ProjectRepository projectRepository;
    private final RiskConfigMapper riskConfigMapper;

    @Override
    @Transactional(readOnly = true)
    public RiskConfigResponse getByProject(UUID projectId) {
        projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(PROJECT_NOT_FOUND));
        return riskConfigRepository.findByProjectId(projectId)
                .map(riskConfigMapper::toResponse)
                .orElseGet(this::defaultConfig);
    }

    @Override
    @Transactional
    public RiskConfigResponse update(UUID projectId, UpdateRiskConfigRequest request, UUID actorId) {
        Project project = projectRepository.findByIdAndDeletedAtIsNull(projectId)
                .orElseThrow(() -> new ResourceNotFoundException(PROJECT_NOT_FOUND));

        RiskConfig config = riskConfigRepository.findByProjectId(projectId)
                .orElseGet(() -> RiskConfig.builder()
                        .project(project)
                        .contingencyMargin(DEFAULT_MARGIN)
                        .build());

        config.setLevel(request.getLevel());
        if (request.getContingencyMargin() != null) {
            config.setContingencyMargin(request.getContingencyMargin());
        }
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy(actorId);

        return riskConfigMapper.toResponse(riskConfigRepository.save(config));
    }

    private RiskConfigResponse defaultConfig() {
        return RiskConfigResponse.builder()
                .level(RiskLevel.MEDIUM)
                .contingencyMargin(DEFAULT_MARGIN)
                .saved(false)
                .build();
    }
}
