package com.intecx.inscope.dto.request.userstory;

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
public class ReorderUserStoriesRequest {

    @NotEmpty(message = "La lista de identificadores de historias es obligatoria")
    private List<UUID> storyIds;
}
