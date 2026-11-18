package com.intecx.stride.dto.response;

import com.intecx.stride.enumeration.CurrencyCode;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record CostAmountResponse(
        BigDecimal amount,
        CurrencyCode currency
) {
}
