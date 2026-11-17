package com.intecx.inscope.service.support;

import com.intecx.inscope.dto.response.CostAmountResponse;
import com.intecx.inscope.dto.response.ProfileCostResponse;
import com.intecx.inscope.dto.response.TaskProfileCostResponse;
import com.intecx.inscope.exception.BadRequestException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfileCostAggregator {

    private final CostAggregator costAggregator;

    public List<ProfileCostResponse> aggregate(Collection<TaskProfileCostResponse> contributions) {
        if (contributions == null || contributions.isEmpty()) {
            throw new BadRequestException("Se requiere al menos una contribución para desglosar los costos");
        }

        Map<UUID, List<CostAmountResponse>> costsByProfile = new LinkedHashMap<>();
        Map<UUID, TaskProfileCostResponse> identityByProfile = new LinkedHashMap<>();
        for (TaskProfileCostResponse contribution : contributions) {
            if (contribution == null) {
                throw new BadRequestException("Las contribuciones no pueden contener elementos nulos");
            }
            if (contribution.technicalProfileId() == null) {
                throw new BadRequestException("El identificador del perfil técnico es obligatorio");
            }
            costsByProfile.computeIfAbsent(contribution.technicalProfileId(), id -> new ArrayList<>())
                    .add(contribution.cost());
            identityByProfile.putIfAbsent(contribution.technicalProfileId(), contribution);
        }
        return costsByProfile.entrySet().stream()
                .map(entry -> {
                    TaskProfileCostResponse identity = identityByProfile.get(entry.getKey());
                    return new ProfileCostResponse(
                            entry.getKey(),
                            identity.profileName(),
                            identity.profileEmail(),
                            identity.profileRole(),
                            costAggregator.subtotal(entry.getValue()));
                })
                .toList();
    }
}
