package com.alekhya.resume_service.dto;

import java.util.List;

public record ResumeAnalysisResponse(
        Long resumeId,
        String filename,
        String professionalSummary,
        List<String> technicalSkills,
        List<String> softSkills,
        List<String> strengths,
        List<String> recommendations
) {
}