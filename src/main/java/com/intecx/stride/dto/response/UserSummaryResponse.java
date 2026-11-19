package com.intecx.stride.dto.response;

import java.util.UUID;
import lombok.Builder;

@Builder
public record UserSummaryResponse(
        UUID id,
        String firstName,
        String lastName,
        String email
) {
}
