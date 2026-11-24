package com.alekhya.resume_service.dto;
import com.alekhya.resume_service.entity.Resume;
import java.time.Instant;

public record ResumeResponse (
    Long id,
    String originalFilename,
    String contentType,
    Long fileSize,
    String status,
    Instant createdAt,
    Instant updatedAt
) {
    public static ResumeResponse from(Resume resume) {
        return new ResumeResponse(
                resume.getId(),
                resume.getOriginalFilename(),
                resume.getContentType(),
                resume.getFileSize(),
                resume.getStatus(),
                resume.getCreatedAt(),
                resume.getUpdatedAt()
        );
    }
}