package com.intecx.stride.service;

import com.intecx.stride.dto.ai.AiAssignmentResponse;
import com.intecx.stride.dto.ai.AiModelMetricsResponse;
import com.intecx.stride.dto.ai.AiRiskPredictionResponse;
import java.util.UUID;

public interface AiEstimationService {

    AiRiskPredictionResponse predictRisk(UUID projectId);

    AiAssignmentResponse optimizeAssignments(UUID projectId, String algorithm, Double maxHoursPerProfile);

    AiModelMetricsResponse getModelMetrics();
}
