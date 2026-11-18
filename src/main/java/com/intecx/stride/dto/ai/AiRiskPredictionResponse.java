package com.intecx.stride.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intecx.stride.enumeration.ClassificationModel;
import com.intecx.stride.enumeration.RegressionModel;
import com.intecx.stride.enumeration.RiskLevel;
import java.math.BigDecimal;
import java.util.Map;

public record AiRiskPredictionResponse(
        @JsonProperty("recommended_contingency_margin") BigDecimal recommendedContingencyMargin,
        @JsonProperty("predicted_risk_level") RiskLevel predictedRiskLevel,
        @JsonProperty("risk_probabilities") Map<String, Double> riskProbabilities,
        @JsonProperty("regression_model_used") RegressionModel regressionModelUsed,
        @JsonProperty("classification_model_used") ClassificationModel classificationModelUsed
) {
}
