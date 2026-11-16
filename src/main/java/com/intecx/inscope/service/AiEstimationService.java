package com.intecx.inscope.service;

import com.intecx.inscope.dto.ai.AiAssignmentResponse;
import com.intecx.inscope.dto.ai.AiModelMetricsResponse;
import com.intecx.inscope.dto.ai.AiRiskPredictionResponse;
import java.util.UUID;

public interface AiEstimationService {

    AiRiskPredictionResponse predictRisk(UUID projectId);

    AiAssignmentResponse optimizeAssignments(UUID projectId, String algorithm, Double maxHoursPerProfile);

    AiModelMetricsResponse getModelMetrics();
}
