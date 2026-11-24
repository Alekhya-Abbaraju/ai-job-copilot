
package com.alekhya.resume_service.kafka.consumer;

import com.alekhya.resume_service.kafka.event.ResumeAnalysisEvent;
import com.alekhya.resume_service.service.ResumeAnalysisService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ResumeAnalysisConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(ResumeAnalysisConsumer.class);

    private final ResumeAnalysisService resumeAnalysisService;

    public ResumeAnalysisConsumer(
            ResumeAnalysisService resumeAnalysisService
    ) {
        this.resumeAnalysisService = resumeAnalysisService;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.resume-analysis-requests}",
            groupId = "resume-analysis-group"
    )
    public void consume(ResumeAnalysisEvent event) {

        log.info(
                "Received resume analysis event. Analysis ID: {}, Resume ID: {}",
                event.analysisId(),
                event.resumeId()
        );

        resumeAnalysisService.processResumeAnalysis(
                event.analysisId(),
                event.userId()
        );
    }
}
