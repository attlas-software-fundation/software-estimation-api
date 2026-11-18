package com.intecx.stride.rest;

import com.intecx.stride.common.QueryRequest;
import com.intecx.stride.dto.response.DashboardSummaryResponse;
import com.intecx.stride.dto.response.SystemReportSummaryResponse;
import com.intecx.stride.service.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard", description = "Indicadores y series para el dashboard del sistema")
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ProjectService projectService;

    @Operation(summary = "Indicadores y series del dashboard según los filtros de proyectos")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dashboard calculado"),
            @ApiResponse(responseCode = "400", description = "Filtros inválidos")
    })
    @PostMapping("/summary")
    @PreAuthorize("hasAuthority('dashboard:read')")
    public ResponseEntity<DashboardSummaryResponse> summarize(
            @RequestBody(required = false) QueryRequest query
    ) {
        SystemReportSummaryResponse summary = projectService.summarize(query);
        boolean projects = has("project:read");
        boolean estimation = has("estimation:read");
        boolean cost = has("cost:read");
        return ResponseEntity.ok(DashboardSummaryResponse.builder()
                .projectCount(projects ? summary.projectCount() : null)
                .projectsWithEstimation(estimation ? summary.projectsWithEstimation() : null)
                .totalStoryPoints(estimation ? summary.totalStoryPoints() : null)
                .totalTimeDays(estimation ? summary.totalTimeDays() : null)
                .totalCost(cost ? summary.totalCost() : null)
                .projectsByStatus(projects ? summary.projectsByStatus() : null)
                .projectsByType(projects ? summary.projectsByType() : null)
                .costByProject(cost ? summary.costByProject() : null)
                .build());
    }

    private boolean has(String authority) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
                .anyMatch(granted -> authority.equals(granted.getAuthority()));
    }
}
