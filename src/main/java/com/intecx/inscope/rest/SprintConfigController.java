package com.intecx.inscope.rest;

import com.intecx.inscope.dto.request.sprint.UpdateSprintConfigRequest;
import com.intecx.inscope.dto.response.SprintConfigResponse;
import com.intecx.inscope.security.UserPrincipal;
import com.intecx.inscope.service.SprintConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Estimaciones", description = "Consolidación, registro y consulta de estimaciones por proyecto")
@RestController
@RequestMapping("/api/v1/projects/{projectId}/sprint-config")
@RequiredArgsConstructor
public class SprintConfigController {

    private final SprintConfigService sprintConfigService;

    @Operation(summary = "Obtener la velocidad del equipo y la duración del sprint de un proyecto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuración obtenida exitosamente"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('estimation:read')")
    public ResponseEntity<SprintConfigResponse> get(@PathVariable UUID projectId) {
        return ResponseEntity.ok(sprintConfigService.getByProject(projectId));
    }

    @Operation(summary = "Actualizar la velocidad del equipo y la duración del sprint de un proyecto")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Configuración actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
    })
    @PutMapping
    @PreAuthorize("hasAuthority('estimation:write')")
    public ResponseEntity<SprintConfigResponse> update(
            @PathVariable UUID projectId,
            @Valid @RequestBody UpdateSprintConfigRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(sprintConfigService.update(projectId, request, principal.getId()));
    }
}