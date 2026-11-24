package com.alekhya.resume_service.dto;

import java.util.List;

public record ResumeInsights(
        String professionalSummary,
        List<String> technicalSkills,
        List<String> softSkills,
        List<String> strengths,
        List<String> recommendations
) {
}