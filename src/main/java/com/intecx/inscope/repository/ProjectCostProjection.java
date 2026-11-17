package com.intecx.inscope.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface ProjectCostProjection {
    UUID getTaskId();
    String getTaskTitle();
    UUID getTechnicalProfileId();
    String getProfileName();
    String getProfileEmail();
    String getProfileRole();
    BigDecimal getEstimateHours();
    BigDecimal getAdjustedHours();
    BigDecimal getHourlyRate();
    String getCurrency();
    Boolean getProfileActive();
    LocalDateTime getProfileDeletedAt();
}
