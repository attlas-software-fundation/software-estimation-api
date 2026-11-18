package com.intecx.stride.dto.request.task;

import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import java.util.UUID;
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
public class ReorderTasksRequest {

    @NotEmpty(message = "La lista de identificadores de tareas es obligatoria")
    private List<UUID> taskIds;
}
