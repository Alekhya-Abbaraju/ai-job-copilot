package com.alekhya.resume_service.kafka.event;

import java.time.Instant;

public record ResumeAnalysisEvent(
        Long analysisId,
        Long resumeId,
        Long userId,
        Instant requestedAt
) {
}
/*
It's simply a message structure.
- resumeId: Which resume needs analysis?
- userId: Who owns that resume?
- requestedAt: When was the request made?
 */