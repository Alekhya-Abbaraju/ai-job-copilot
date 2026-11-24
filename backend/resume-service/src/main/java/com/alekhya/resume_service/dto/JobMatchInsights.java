package com.alekhya.resume_service.dto;

import java.util.List;

public record JobMatchInsights(
        Integer matchScore,
        List<String> matchingSkills,
        List<String> missingSkills,
        List<String> matchedRequirements,
        List<String> experienceGaps,
        List<String> recommendations
) {
}