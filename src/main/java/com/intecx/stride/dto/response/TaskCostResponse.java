package com.intecx.stride.dto.response;

import java.util.List;
import java.util.UUID;
import lombok.Builder;

@Builder
public record TaskCostResponse(
        UUID taskId,
        String taskTitle,
        CostAmountResponse total,
        List<TaskProfileCostResponse> contributions
) {
}
