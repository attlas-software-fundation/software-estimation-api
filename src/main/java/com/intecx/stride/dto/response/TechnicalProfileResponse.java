package com.intecx.stride.dto.response;

import com.intecx.stride.enumeration.CurrencyCode;
import com.intecx.stride.enumeration.ExperienceLevel;
import com.intecx.stride.enumeration.TechnicalRole;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record TechnicalProfileResponse(
        UUID id,
        String name,
        String email,
        TechnicalRole role,
        ExperienceLevel experienceLevel,
        BigDecimal hourlyRate,
        CurrencyCode currency,
        boolean isActive,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
