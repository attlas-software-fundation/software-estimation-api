package com.intecx.stride.dto.request.sprint;

import com.intecx.stride.enumeration.SprintUnit;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSprintConfigRequest {

    @NotNull(message = "La velocidad del equipo es obligatoria")
    @Min(value = 1, message = "La velocidad debe ser al menos 1")
    @Max(value = 1000, message = "La velocidad no puede superar 1000")
    private Integer velocity;

    @NotNull(message = "La duración del sprint es obligatoria")
    @Min(value = 1, message = "La duración debe ser al menos 1")
    @Max(value = 365, message = "La duración no puede superar 365")
    private Integer duration;

    @NotNull(message = "La unidad del sprint es obligatoria")
    private SprintUnit unit;
}