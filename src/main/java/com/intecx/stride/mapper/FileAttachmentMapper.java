package com.intecx.stride.mapper;

import com.intecx.stride.dto.response.FileAttachmentResponse;
import com.intecx.stride.entity.FileAttachment;
import org.springframework.stereotype.Component;

@Component
public class FileAttachmentMapper implements BaseMapper<FileAttachment, FileAttachmentResponse> {

    @Override
    public FileAttachmentResponse toResponse(FileAttachment entity) {
        if (entity == null) {
            return null;
        }
        return FileAttachmentResponse.builder()
                .id(entity.getId())
                .type(entity.getType())
                .fileName(entity.getFileName())
                .contentType(entity.getContentType())
                .sizeBytes(entity.getSizeBytes())
                .storagePath(entity.getStoragePath())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
