package com.intecx.stride.service.impl;

import com.intecx.stride.common.PaginatedResponse;
import com.intecx.stride.common.PaginationConstants;
import com.intecx.stride.common.PaginationRequest;
import com.intecx.stride.common.QueryRequest;
import com.intecx.stride.dto.response.FileAttachmentResponse;
import com.intecx.stride.dto.response.ProjectResponse;
import com.intecx.stride.entity.FileAttachment;
import com.intecx.stride.entity.ReportSnapshot;
import com.intecx.stride.entity.Task;
import com.intecx.stride.entity.TechnicalProfile;
import com.intecx.stride.entity.UserStory;
import com.intecx.stride.enumeration.AttachmentType;
import com.intecx.stride.exception.ResourceNotFoundException;
import com.intecx.stride.mapper.FileAttachmentMapper;
import com.intecx.stride.repository.FileAttachmentRepository;
import com.intecx.stride.repository.ReportSnapshotRepository;
import com.intecx.stride.repository.TaskRepository;
import com.intecx.stride.repository.UserStoryRepository;
import com.intecx.stride.service.BunnyStorageService;
import com.intecx.stride.service.ProjectService;
import com.intecx.stride.service.ReportService;
import com.intecx.stride.service.support.BunnyPaths;
import com.intecx.stride.service.support.EstimationPdfGenerator;
import com.intecx.stride.service.support.PdfStorySection;
import com.intecx.stride.service.support.PdfTaskLine;
import com.intecx.stride.service.support.ProjectsExcelExporter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private static final String ESTIMATION_NOT_FOUND = "Estimación no encontrada";
    private static final String PDF_NOT_FOUND = "PDF de la estimación no encontrado";
    private static final String PDF_CONTENT_TYPE = MediaType.APPLICATION_PDF_VALUE;

    private final ReportSnapshotRepository reportSnapshotRepository;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final BunnyStorageService bunnyStorageService;
    private final ProjectService projectService;
    private final UserStoryRepository userStoryRepository;
    private final TaskRepository taskRepository;
    private final FileAttachmentMapper fileAttachmentMapper;

    @Override
    @Transactional
    public FileAttachmentResponse generateEstimationPdf(UUID projectId, UUID estimationId, UUID actorId) {
        ReportSnapshot snapshot = requireSnapshot(projectId, estimationId);
        byte[] pdf = EstimationPdfGenerator.generate(snapshot, storySections(projectId));
        String fileName = estimationId + ".pdf";
        String storagePath = BunnyPaths.reportPdf(estimationId);

        bunnyStorageService.upload(storagePath, pdf, PDF_CONTENT_TYPE);

        FileAttachment previous = snapshot.getFile();
        if (previous != null) {
            snapshot.setFile(null);
            reportSnapshotRepository.saveAndFlush(snapshot);
            if (!storagePath.equals(previous.getStoragePath())) {
                bunnyStorageService.delete(previous.getStoragePath());
            }
            fileAttachmentRepository.delete(previous);
            fileAttachmentRepository.flush();
        }

        FileAttachment saved = fileAttachmentRepository.save(FileAttachment.builder()
                .type(AttachmentType.PDF)
                .fileName(fileName)
                .contentType(PDF_CONTENT_TYPE)
                .sizeBytes((long) pdf.length)
                .storagePath(storagePath)
                .createdAt(LocalDateTime.now())
                .createdBy(actorId)
                .build());

        snapshot.setFile(saved);
        reportSnapshotRepository.save(snapshot);
        return fileAttachmentMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadEstimationPdf(UUID projectId, UUID estimationId) {
        ReportSnapshot snapshot = requireSnapshot(projectId, estimationId);
        if (snapshot.getFile() == null) {
            throw new ResourceNotFoundException(PDF_NOT_FOUND);
        }
        return bunnyStorageService.download(snapshot.getFile().getStoragePath());
    }

    @Override
    @Transactional(readOnly = true)
    public String estimationPdfFileName(UUID projectId, UUID estimationId) {
        return EstimationPdfGenerator.fileName(requireSnapshot(projectId, estimationId).getProjectName());
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] exportProjectsExcel() {
        List<ProjectResponse> projects = new ArrayList<>();
        int pageNumber = PaginationConstants.FIRST_PAGE;
        boolean hasNext;
        do {
            PaginatedResponse<ProjectResponse> page = projectService.search(new QueryRequest(
                    null,
                    new PaginationRequest("", PaginationConstants.MAX_PAGE_SIZE, pageNumber, null)
            ));
            projects.addAll(page.getItems());
            hasNext = page.getMeta().hasNext();
            pageNumber++;
        } while (hasNext);
        return ProjectsExcelExporter.export(projects);
    }

    private List<PdfStorySection> storySections(UUID projectId) {
        List<UserStory> stories = userStoryRepository.findByProject_IdAndDeletedAtIsNull(projectId).stream()
                .sorted(Comparator.comparing(UserStory::getPosition))
                .toList();
        List<Task> tasks = taskRepository.findByStory_Project_IdAndDeletedAtIsNull(projectId);
        List<PdfStorySection> sections = new ArrayList<>();
        for (UserStory story : stories) {
            List<PdfTaskLine> lines = tasks.stream()
                    .filter(task -> task.getStory().getId().equals(story.getId()))
                    .sorted(Comparator.comparing(Task::getPosition))
                    .map(task -> new PdfTaskLine(
                            task.getTitle(),
                            profileNames(task),
                            task.getEstimateHours() == null ? 0 : task.getEstimateHours()
                    ))
                    .toList();
            sections.add(new PdfStorySection(
                    story.getCode(),
                    story.getTitle(),
                    story.getStoryPoints() == null ? null : story.getStoryPoints().intValue(),
                    lines
            ));
        }
        return sections;
    }

    private String profileNames(Task task) {
        if (task.getResponsibleProfiles() == null || task.getResponsibleProfiles().isEmpty()) {
            return "Sin perfil";
        }
        return task.getResponsibleProfiles().stream()
                .map(TechnicalProfile::getName)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .reduce((left, right) -> left + ", " + right)
                .orElse("Sin perfil");
    }

    private ReportSnapshot requireSnapshot(UUID projectId, UUID estimationId) {
        return reportSnapshotRepository
                .findByIdAndProject_IdAndProject_DeletedAtIsNull(estimationId, projectId)
                .orElseThrow(() -> new ResourceNotFoundException(ESTIMATION_NOT_FOUND));
    }
}
