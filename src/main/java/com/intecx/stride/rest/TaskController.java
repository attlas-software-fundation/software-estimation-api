package com.intecx.stride.rest;

import com.intecx.stride.dto.request.task.CreateTaskRequest;
import com.intecx.stride.dto.request.task.ReorderTasksRequest;
import com.intecx.stride.dto.request.task.UpdateTaskRequest;
import com.intecx.stride.dto.response.TaskResponse;
import com.intecx.stride.enumeration.TaskStatus;
import com.intecx.stride.security.UserPrincipal;
import com.intecx.stride.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Tareas", description = "Endpoints para la gestión de tareas y perfiles responsables")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @Operation(summary = "Listar tareas de una historia con filtros opcionales")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Historia no encontrada")
    })
    @GetMapping("/user-stories/{storyId}/tasks")
    @PreAuthorize("hasAuthority('backlog:read')")
    public ResponseEntity<List<TaskResponse>> listByStory(
            @PathVariable UUID storyId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TaskStatus status
    ) {
        return ResponseEntity.ok(taskService.listByStory(storyId, search, status));
    }

    @Operation(summary = "Listar tareas de un proyecto con filtros opcionales")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listado obtenido exitosamente"),
            @ApiResponse(responseCode = "404", description = "Proyecto no encontrado")
    })
    @GetMapping("/projects/{projectId}/tasks")
    @PreAuthorize("hasAuthority('backlog:read')")
    public ResponseEntity<List<TaskResponse>> listByProject(
            @PathVariable UUID projectId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) TaskStatus status
    ) {
        return ResponseEntity.ok(taskService.listByProject(projectId, search, status));
    }

    @Operation(summary = "Crear una tarea dentro de una historia")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarea creada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Historia o perfil no encontrado")
    })
    @PostMapping("/user-stories/{storyId}/tasks")
    @PreAuthorize("hasAuthority('backlog:write')")
    public ResponseEntity<TaskResponse> create(
            @PathVariable UUID storyId,
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        TaskResponse response = taskService.create(storyId, request, principal.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Obtener una tarea por su ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarea encontrada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    @GetMapping("/tasks/{id}")
    @PreAuthorize("hasAuthority('backlog:read')")
    public ResponseEntity<TaskResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.findById(id));
    }

    @Operation(summary = "Actualizar una tarea existente")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tarea actualizada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    @PutMapping("/tasks/{id}")
    @PreAuthorize("hasAuthority('backlog:write')")
    public ResponseEntity<TaskResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ResponseEntity.ok(taskService.update(id, request, principal.getId()));
    }

    @Operation(summary = "Reordenar las tareas de una historia de usuario")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tareas reordenadas exitosamente"),
            @ApiResponse(responseCode = "400", description = "Lista de identificadores inválida"),
            @ApiResponse(responseCode = "404", description = "Historia no encontrada")
    })
    @PutMapping("/user-stories/{storyId}/tasks/reorder")
    @PreAuthorize("hasAuthority('backlog:write')")
    public ResponseEntity<Void> reorder(
            @PathVariable UUID storyId,
            @Valid @RequestBody ReorderTasksRequest request,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        taskService.reorder(storyId, request.getTaskIds(), principal.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Eliminar una tarea (borrado lógico)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Tarea eliminada exitosamente"),
            @ApiResponse(responseCode = "404", description = "Tarea no encontrada")
    })
    @DeleteMapping("/tasks/{id}")
    @PreAuthorize("hasAuthority('backlog:delete')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        taskService.delete(id, principal.getId());
        return ResponseEntity.noContent().build();
    }
}
