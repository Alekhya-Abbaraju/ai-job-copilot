package com.alekhya.resume_service.dto;

import java.time.Instant;
import java.util.List;

public record ResumeAnalysisResultResponse(
        Long analysisId,
        Long resumeId,
        String status,
        String professionalSummary,
        List<String> technicalSkills,
        List<String> softSkills,
        List<String> strengths,
        List<String> recommendations,
        String errorMessage,
        Instant createdAt,
        Instant updatedAt
) {}