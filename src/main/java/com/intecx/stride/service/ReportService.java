package com.intecx.stride.service;

import com.intecx.stride.dto.response.FileAttachmentResponse;
import java.util.UUID;

public interface ReportService {

    FileAttachmentResponse generateEstimationPdf(UUID projectId, UUID estimationId, UUID actorId);

    byte[] downloadEstimationPdf(UUID projectId, UUID estimationId);

    String estimationPdfFileName(UUID projectId, UUID estimationId);

    byte[] exportProjectsExcel();
}
