package com.intecx.inscope.dto.ai;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.intecx.inscope.enumeration.ClassificationModel;
import com.intecx.inscope.enumeration.RegressionModel;
import java.util.List;
import java.util.Map;

public record AiModelMetricsResponse(
        @JsonProperty("selected_regression_model") RegressionModel selectedRegressionModel,
        @JsonProperty("selected_classification_model") ClassificationModel selectedClassificationModel,
        @JsonProperty("regression_comparison") Map<RegressionModel, RegressionMetric> regressionComparison,
        @JsonProperty("classification_comparison") Map<ClassificationModel, ClassificationMetric> classificationComparison,
        @JsonProperty("dataset_rows") int datasetRows
) {
    public record RegressionMetric(
            double rmse,
            double mae,
            double r2,
            @JsonProperty("best_params") Map<String, Object> bestParams
    ) {
    }

    public record ClassificationMetric(
            @JsonProperty("is_ensemble") boolean isEnsemble,
            double accuracy,
            @JsonProperty("precision_weighted") double precisionWeighted,
            @JsonProperty("recall_weighted") double recallWeighted,
            @JsonProperty("f1_weighted") double f1Weighted,
            @JsonProperty("roc_auc_ovr") double rocAucOvr,
            @JsonProperty("best_params") Map<String, Object> bestParams,
            List<String> labels,
            @JsonProperty("confusion_matrix") List<List<Integer>> confusionMatrix
    ) {
    }
}
