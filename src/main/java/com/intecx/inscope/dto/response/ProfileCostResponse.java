package com.intecx.inscope.dto.response;

import java.util.UUID;
import lombok.Builder;

@Builder
public record ProfileCostResponse(
        UUID technicalProfileId,
        String profileName,
        String profileEmail,
        String profileRole,
        CostAmountResponse subtotal
) {
}
