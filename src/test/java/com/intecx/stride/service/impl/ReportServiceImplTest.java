package com.intecx.stride.service.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.intecx.stride.entity.FileAttachment;
import com.intecx.stride.entity.ReportSnapshot;
import com.intecx.stride.exception.ResourceNotFoundException;
import com.intecx.stride.mapper.FileAttachmentMapper;
import com.intecx.stride.repository.FileAttachmentRepository;
import com.intecx.stride.repository.ReportSnapshotRepository;
import com.intecx.stride.repository.TaskRepository;
import com.intecx.stride.repository.UserStoryRepository;
import com.intecx.stride.service.BunnyStorageService;
import com.intecx.stride.service.ProjectService;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    private static final UUID PROJECT_ID = UUID.fromString("00000000-0000-0000-0000-000000000011");
    private static final UUID ESTIMATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000012");

    @Mock
    private ReportSnapshotRepository reportSnapshotRepository;
    @Mock
    private FileAttachmentRepository fileAttachmentRepository;
    @Mock
    private BunnyStorageService bunnyStorageService;
    @Mock
    private ProjectService projectService;
    @Mock
    private UserStoryRepository userStoryRepository;
    @Mock
    private TaskRepository taskRepository;
    @Mock
    private FileAttachmentMapper fileAttachmentMapper;

    private ReportServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ReportServiceImpl(
                reportSnapshotRepository,
                fileAttachmentRepository,
                bunnyStorageService,
                projectService,
                userStoryRepository,
                taskRepository,
                fileAttachmentMapper
        );
    }

    @Test
    void downloadEstimationPdfReturnsBytesFromBunny() {
        FileAttachment file = FileAttachment.builder()
                .storagePath("reports/" + ESTIMATION_ID + ".pdf")
                .build();
        ReportSnapshot snapshot = ReportSnapshot.builder().file(file).build();
        when(reportSnapshotRepository.findByIdAndProject_IdAndProject_DeletedAtIsNull(ESTIMATION_ID, PROJECT_ID))
                .thenReturn(Optional.of(snapshot));
        when(bunnyStorageService.download(file.getStoragePath())).thenReturn(new byte[] {1, 2, 3});

        byte[] result = service.downloadEstimationPdf(PROJECT_ID, ESTIMATION_ID);

        assertArrayEquals(new byte[] {1, 2, 3}, result);
        verify(bunnyStorageService).download(file.getStoragePath());
    }

    @Test
    void downloadEstimationPdfThrowsWhenFileIsMissing() {
        when(reportSnapshotRepository.findByIdAndProject_IdAndProject_DeletedAtIsNull(ESTIMATION_ID, PROJECT_ID))
                .thenReturn(Optional.of(ReportSnapshot.builder().build()));

        assertThrows(ResourceNotFoundException.class,
                () -> service.downloadEstimationPdf(PROJECT_ID, ESTIMATION_ID));
    }
}
