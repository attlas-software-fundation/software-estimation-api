package com.intecx.stride.dto.request.project;

import com.intecx.stride.enumeration.ProjectStatus;
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
public class UpdateProjectStatusRequest {

    @NotNull(message = "El estado del proyecto es obligatorio")
    private ProjectStatus status;
}
