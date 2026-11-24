
package com.alekhya.resume_service.dto;

import java.time.Instant;

public record ResumeAnalysisRequestResponse(
        Long analysisId,
        Long resumeId,
        String status,
        Instant requestedAt,
        String message
) {
}
