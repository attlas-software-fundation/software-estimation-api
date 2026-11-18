package com.intecx.stride.dto.response;

import com.intecx.stride.enumeration.AttachmentType;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record FileAttachmentResponse(
        UUID id,
        AttachmentType type,
        String fileName,
        String contentType,
        Long sizeBytes,
        String storagePath,
        LocalDateTime createdAt
) {
}
